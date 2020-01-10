import socket
import json

class client:
    ip = ''
    ip_LB = '127.0.0.1'
    port_LB = 6969
    ip_replica = ''
    port_replica = 8181

    def __init__(self):
        self.ip = socket.gethostbyname(socket.gethostname())

    def connect_lb(self):
        # connect to LB
        Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_lb.connect((self.ip_LB, self.port_LB))
        except ConnectionRefusedError:
            print('Load Balancer is closed!\n')
            return

        # send request
        sendData = {'method' : 'CONNECT' ,
                       'resource' : 'ip' ,
                       'content' : self.ip}
        Socket_lb.send(((json.dumps(sendData)+'\n').encode('utf-8')))
        print(str((json.dumps(sendData)+'\n').encode('utf-8')))

        # receive data
        recv_data = json.loads(Socket_lb.recv(1024).decode('utf-8'))
        self.ip_replica = recv_data['content']
        print('Successfully connect to load balancer!\nYour replica\'s ip is ' + self.ip_replica)
        Socket_lb.close()

    def check_lb(self):
        # Connect to LB
        Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_lb.connect((self.ip_LB, self.port_LB))
        except ConnectionRefusedError:
            print('Load Balancer is closed!\n')
            return

        # send request to loadBalancer
        sendData = {'method' : 'CHECK' ,
                     'resource' : self.ip_replica}
        Socket_lb.send(json.dumps(sendData))

        # receive data
        recv_data = json.loads(Socket_lb.recv(1024))
        replica_status = recv_data['content']
        Socket_lb.close()
        return replica_status  #  I'm not sure the return type is boolean or string, it should be boolean

    def dealWithErr_rp(self):
        print('Cannot connect to your replica, But you need to stick to it~~~\n'
              'Don\'t worry, we will consult load balancer the status of your replica\n')
        if self.check_lb():
            # means the LB didn't received down message from replica, so we let user try again
            print('Your replica seems still alive, try later!\n')
        else:
            # means the replica has deleted itself, apply a new replica
            print('Oops! Your former replica betrayed you! We are reallocating a new loyal replica to you!\n')
            self.connect_lb()

    def read_rp(self , key4search):
        # Connect to replica
        Socket_replica = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_replica.connect((self.ip_replica, self.port_replica))
        except ConnectionRefusedError:
            self.dealWithErr_rp()

        # Send request
        sendData = {'method' : 'READ',
                    'resource' : self.ip ,
                'content' : key4search}
        Socket_replica.send(json.dumps(sendData))

        # receive data
        recv_data = json.loads(Socket_replica.recv(1024))
        content = recv_data['content']
        print(content)
        Socket_replica.close()

    def write_rp(self, what2write):
        # Connect to replica
        Socket_replica = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_replica.connect((self.ip_replica, self.port_replica))
        except ConnectionRefusedError:
            self.dealWithErr_rp()

        # Send request
        sendData = {'method': 'WRITE',
                    'resource': self.ip,
                    'content': what2write}
        Socket_replica.send(json.dumps(sendData))

        # receive data
        recv_data = json.loads(Socket_replica.recv(1024))
        content = recv_data['content']
        print(content)
        Socket_replica.close()

    def delete_rp(self, key2delete):
        # Connect to replica
        Socket_replica = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            Socket_replica.connect((self.ip_replica, self.port_replica))
        except ConnectionRefusedError:
            self.dealWithErr_rp()

        # Send request
        sendData = {'method': 'DELETE',
                    'resource': self.ip,
                    'content': key2delete}
        Socket_replica.send(json.dumps(sendData))

        # receive data
        recv_data = json.loads(Socket_replica.recv(1024))
        content = recv_data['content']
        print(content)
        Socket_replica.close()


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
