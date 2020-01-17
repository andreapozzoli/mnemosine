import socket
import json
from client_UI import *

class clientStick:
    ip = ''
    ip_LB = '10.169.212.112'
    port_LB = 6969
    ip_replica = ''
    port_replica = 0

    def __init__(self):
        self.ip = socket.gethostbyname(socket.gethostname())

    def connect_lb(self , text_ipReplica):
        # connect to LB
        Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_lb.connect((self.ip_LB, self.port_LB))
        except ConnectionRefusedError:
            text_ipReplica.delete(0.0, END)
            text_ipReplica.insert(END , 'Load Balancer is closed!\n')
            return

        # send request
        sendData = {'method' : 'CONNECT' ,
                       'resource' : 'ip' ,
                       'content' : self.ip}
        Socket_lb.send(((json.dumps(sendData)+'\n').encode('utf-8')))
        #print(str((json.dumps(sendData)+'\n').encode('utf-8')))

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
        Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_lb.connect((self.ip_LB, self.port_LB))
        except ConnectionRefusedError:
            text_out.insert(END , 'Load Balancer is closed!\n')
            return

        # send request to loadBalancer
        sendData = {'method' : 'CHECK' ,
                     'resource' : self.ip_replica}
        Socket_lb.send((json.dumps(sendData)+'\n').encode('utf-8'))

        # receive data
        recv_data = json.loads(Socket_lb.recv(1024).decode('utf-8'))
        replica_status = recv_data['content']
        Socket_lb.close()
        return replica_status #I'm not sure the return type is boolean or string, it should be boolean

    def dealWithErr_rp(self , text_out):
        text_out.insert(END ,'Cannot connect to your replica, But you need to stick to it~~~\n'
              'Don\'t worry, we will consult load balancer the status of your replica\n')
        if self.check_lb(text_out) == "true":
            # means the LB didn't received down message from replica, so we let user try again
            text_out.insert(END , 'Your replica seems still alive, try later!\n\n')
        else:
            # means the replica has deleted itself, apply a new replica
            text_out.insert(END , 'Oops! Your former replica betrayed you! Try to get a new loyal replica!\n\n')

    def read_rp(self , text_in , text_out):
        # Send request
        sendData = {'method' : 'READ',
                    'resource' : text_in.get('1.0', END) ,
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
        text_out.insert(END , content+'\n\n')

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
        text_out.insert(END , content+'\n\n')

    def delete_rp(self, text_in , text_out):
        # Send request
        sendData = {'method': 'DELETE',
                    'resource': text_in.get('1.0', END),
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
        text_out.insert(END , content+'\n\n')

'''
if __name__ == '__main__':
    # create an object
    client_obj = client()
    # connect to load balancer to get replica's ip
    client_obj.connect_lb()
    # write something
    client_obj.write_rp({'I_am_key':'I_am_value'})
    # read it
    client_obj.read_rp('I_am_key')
    # delete it
    client_obj.delete_rp('I_am_key')
'''
