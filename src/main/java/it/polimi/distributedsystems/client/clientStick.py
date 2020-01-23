import socket
import json
from client_UI import *

class clientStick:
    ip = ''
    ip_LB = '127.0.0.1'
    port_LB = 6969
    ip_replica = ''
    port_replica = 0

    def __init__(self):
        self.ip = socket.gethostbyname(socket.gethostname())

    def connect_lb(self , text_ipReplica):
        # connect to LB
        print('connecting to Load Balancer @ ' +self.ip_LB +' : ' +str(self.port_LB) +' to apply for a replica\n')
        Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_lb.connect((self.ip_LB, self.port_LB))
        except ConnectionRefusedError:
            print('cannot connect to Load Balancer!\n')
            text_ipReplica.delete(0.0, END)
            text_ipReplica.insert(END , 'Load Balancer is closed!\n')
            return

        # send request
        sendData = {'method' : 'CONNECT' ,
                       'resource' : 'ip' ,
                       'content' : self.ip}
        Socket_lb.send(((json.dumps(sendData)+'\n').encode('utf-8')))

        # receive data
        recv_data = json.loads(Socket_lb.recv(1024).decode('utf-8'))
        recv_data = recv_data['content'].split(':')
        self.ip_replica = recv_data[0]
        self.port_replica = eval(recv_data[1])
        text_ipReplica.delete(0.0, END)
        text_ipReplica.insert(END , self.ip_replica+" : "+str(self.port_replica)+'\n\n')
        global Socket_replica
        Socket_replica = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_replica.connect((self.ip_replica, self.port_replica))
        except ConnectionRefusedError:
            self.dealWithErr_rp(text_ipReplica)
            return
        Socket_lb.close()

    def check_lb(self , text_out):
        # Connect to LB
        print('client cannot connect to its replica, checking status to LB now\n')
        Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_lb.connect((self.ip_LB, self.port_LB))
        except ConnectionRefusedError:
            text_out.insert(END , 'Load Balancer is closed!\n')
            return

        # send request to loadBalancer
        sendData = {'method' : 'CHECK' ,
                     'resource' : self.ip_replica + ":" + str(self.port_replica)}
        Socket_lb.send((json.dumps(sendData)+'\n').encode('utf-8'))

        # receive data
        recv_data = json.loads(Socket_lb.recv(1024).decode('utf-8'))
        replica_status = recv_data['content']
        Socket_lb.close()
        return replica_status

    def dealWithErr_rp(self , text_out):
        text_out.insert(END ,'Cannot connect to your replica, But you need to stick to it~~~\n'
              'Don\'t worry, we will consult load balancer the status of your replica\n')
        status = self.check_lb(text_out)
        print('The status of client\'s replica is '+status)
        if status == "true":
            # means the LB didn't received down message from replica, so we let user try again
            text_out.insert(END , 'Your replica seems still alive, try later!\n\n')
        elif status == "false":
            # means the replica has deleted itself, apply a new replica
            text_out.insert(END , 'Oops! Your former replica betrayed you! Try to get a new loyal replica!\n\n')
        else:
            text_out.insert(END , 'Unknown status '+ str(status) + '\n\n')

    def read_rp(self , text_in , text_out):
        # Send request
        what2read = text_in.get('1.0', END)
        sendData = {'method' : 'READ',
                    'resource' : what2read ,
                'content' : self.ip}
        try:
            Socket_replica.send((json.dumps(sendData) + '\n').encode('utf-8'))
        except:
            self.dealWithErr_rp(text_out)
            return

        # receive data
        recv_data = json.loads(Socket_replica.recv(1024).decode('utf-8'))
        Socket_replica.recv(1024)
        content = recv_data['content']
        text_out.insert(END , 'READ -> key<' +what2read[:-1] +' >\'s value is < ' +content +' >\n\n')
        print('A read operation for '+what2read[:-1] +' @ ' +self.ip +' finished\n')

    def write_rp(self, text_in , text_out):
        # Send request
        keyValue = text_in.get('1.0', END).split(',')
        key = keyValue[0]
        value = keyValue[1]
        sendData = {'method': 'WRITE',
                    'resource': key,
                    'content': value}
        try:
            Socket_replica.send((json.dumps(sendData) + '\n').encode('utf-8'))
        except:
            self.dealWithErr_rp(text_out)
            return

        # receive data
        recv_data = json.loads(Socket_replica.recv(1024).decode('utf-8'))
        Socket_replica.recv(1024)
        content = recv_data['content']
        text_out.insert(END , 'WRITE -> key< ' +key +' > value< ' +value[:-1] +' > is '+content+'\n\n')
        print('A write operation for ' + key + ' @ ' + self.ip + ' finished\n')

    def delete_rp(self, text_in , text_out):
        # Send request
        what2delete = text_in.get('1.0', END)
        sendData = {'method': 'DELETE',
                    'resource': what2delete,
                    'content': self.ip}
        try:
            Socket_replica.send((json.dumps(sendData) + '\n').encode('utf-8'))
        except:
            self.dealWithErr_rp(text_out)
            return

        # receive data
        recv_data = json.loads(Socket_replica.recv(1024).decode('utf-8'))
        Socket_replica.recv(1024)
        content = recv_data['content']
        text_out.insert(END , 'DELETE -> key< ' +what2delete[:-1] +' > is ' +content+'\n\n')
        print('A delete operation for ' + what2delete[:-1] + ' @ ' + self.ip + ' finished\n')