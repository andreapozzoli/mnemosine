import socket
import json
from tkinter import *

class clientStick:
    ip = ''
    ip_LB = '192.168.20.108'
    port_LB = 6969
    ip_replica = ''
    port_replica = 0

    def __init__(self):
        self.ip = socket.gethostbyname(socket.gethostname())
        self.debug = True
        self.sticky = False

    def connect_lb(self, text_ipReplica):
        # connect to LB
        if self.debug or not self.sticky:
            print('connecting to Load Balancer @ ' + self.ip_LB + ' : ' + str(self.port_LB) + ' to apply for a replica\n')
            Socket_lb = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            try:
                Socket_lb.connect((self.ip_LB, self.port_LB))
            except ConnectionRefusedError:
                print('cannot connect to Load Balancer!\n')
                text_ipReplica.delete(0.0, END)
                text_ipReplica.insert(END, 'Load Balancer is closed!\n')
                return

            # send request
            sendData = {'method': 'CONNECT',
                        'resource': 'ip',
                        'content': self.ip}
            Socket_lb.send(((json.dumps(sendData)+'\n').encode('utf-8')))

            # receive data
            recv_data = json.loads(Socket_lb.recv(1024).decode('utf-8'))
            recv_data = recv_data['content'].split(':')
            self.ip_replica = recv_data[0]
            self.port_replica = eval(recv_data[1])
            text_ipReplica.delete(0.0, END)
            text_ipReplica.insert(END, self.ip_replica+" : "+str(self.port_replica)+'\n\n')
            global Socket_replica
            Socket_replica = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            try:
                Socket_replica.connect((self.ip_replica, self.port_replica))
                self.sticky = True
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
        sendData = {'method': 'CHECK',
                    'resource': self.ip_replica + ":" + str(self.port_replica)}
        Socket_lb.send((json.dumps(sendData)+'\n').encode('utf-8'))

        # receive data
        recv_data = json.loads(Socket_lb.recv(1024).decode('utf-8'))
        replica_status = recv_data['content']
        Socket_lb.close()
        return replica_status

    def dealWithErr_rp(self, text_out):
        text_out.insert(END, 'Cannot connect to your replica, But you need to stick to it~~~\n'
                             'Don\'t worry, we will consult load balancer the status of your replica\n')
        status = self.check_lb(text_out)
        print('The status of client\'s replica is '+status)
        if status == "true":
            # means the LB didn't received down message from replica, so we let user try again
            text_out.insert(END, 'Your replica seems still alive, try later!\n\n')
        elif status == "false":
            # means the replica has deleted itself, apply a new replica
            text_out.insert(END, 'Oops! Your former replica betrayed you! Try to get a new loyal replica!\n\n')
            self.sticky = False
        else:
            text_out.insert(END, 'Unknown status ' + str(status) + '\n\n')

    def read_rp(self, text_in, text_out):
        # Send request
        what2read = text_in.get('1.0', END)
        sendData = {'method': 'READ',
                    'resource': what2read,
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
        text_out.insert(END, 'READ -> key<' + what2read[:-1] + ' >\'s value is < ' + content + ' >\n\n')
        print('A read operation for '+what2read[:-1] + ' @ ' + self.ip + ' finished\n')

    def write_rp(self, text_in, text_out):
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
        text_out.insert(END, 'Performing a write operation... this could take a while\n')
        text_out.insert(END, 'If the program freeze, please, don\'t panic, it is normal\n')
        recv_data = json.loads(Socket_replica.recv(1024).decode('utf-8'))
        Socket_replica.recv(1024)
        content = recv_data['content']
        text_out.insert(END, 'WRITE -> key< ' + key + ' > value< ' + value[:-1] + ' > is ' + content+'\n\n')
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
        text_out.insert(END, 'Performing a delete operation... this could take a while\n')
        text_out.insert(END, 'If the program freeze, please, don\'t panic, it is normal\n')
        recv_data = json.loads(Socket_replica.recv(1024).decode('utf-8'))
        Socket_replica.recv(1024)
        content = recv_data['content']
        text_out.insert(END, 'DELETE -> key< ' + what2delete[:-1] +' > is ' + content + '\n\n')
        print('A delete operation for ' + what2delete[:-1] + ' @ ' + self.ip + ' finished\n')

class client_UI:

    def __init__(self):
        # Create an object
        client_obj = clientStick()
        # initialize Tk（），now we get a window
        root = Tk()
        self.createWindow(root)
        self.setTopFrame(root, client_obj)
        self.setBottomFrame(root, client_obj)
        # keep refreshing
        root.mainloop()

    def createWindow(self , root):
        # set the tile of the window
        root.title("client")
        # set the size of the window
        root.geometry('1280x720')
        # set the size of window changable
        root.resizable()

    def setTopFrame(self, root, client_obj):
        # At the top of the window, there is a frame to show the ip_replica and a button for CONNECT
        frame_top = Frame(root)
        frame_topLeft = Frame(frame_top)
        frame_topRight = Frame(frame_top)
        global text_ipReplica  #  for ip_replica needs to be access to more than one function, it should be a global var
        text_ipReplica = Text(frame_topLeft, width=30, height=1, font=("Times New Roman", 10))
        label_topLeft = Label(frame_topLeft, text="IP of your replica: ")
        # add button for CONNECT
        Button(frame_topRight, text="CONNECT", command=lambda: (client_obj.connect_lb(text_ipReplica))).pack()

        # show them
        text_ipReplica.pack(side=RIGHT)
        label_topLeft.pack(side=LEFT)
        frame_topLeft.pack(side=LEFT)
        frame_topRight.pack(side=RIGHT)
        frame_top.pack(side=TOP)

    def setBottomFrame(self, root, client_obj):
        # Bottom frame has 3 parts, from left to right, which are input(frame_bottemLeft), output, button bar(frame_bottomRight)
        # initialize frame
        frame_bottom = Frame(root)
        frame_bLeft = Frame(frame_bottom)
        frame_bRight = Frame(frame_bottom)
        frame_bRL = Frame(frame_bRight)
        frame_bRR = Frame(frame_bRight)
        # set label for two text bars
        label_bLeft = Label(frame_bLeft, text="Input:")
        label_bRL = Label(frame_bRL, text="Output:")
        global text_in
        text_in = Text(frame_bLeft, width=80, height=38, font=("Times New Roman", 10))
        global text_out
        text_out = Text(frame_bRL, width=80, height=38, font=("Times New Roman", 10))
        # set button for button bar
        label_bRR = Label(frame_bRR, text="Functions:")
        button_READ = Button(frame_bRR, text="READ", command=lambda: (client_obj.read_rp(text_in , text_out)))
        button_WRITE = Button(frame_bRR, text="WRITE", command=lambda: (client_obj.write_rp(text_in, text_out)))
        button_DELETE = Button(frame_bRR, text="DELETE", command=lambda: (client_obj.delete_rp(text_in, text_out)))
        # pack little widgets
        label_bLeft.pack(side=TOP)
        label_bRL.pack(side=TOP)
        text_in.pack(side=TOP)
        text_out.pack(side=TOP)
        label_bRR.pack(side=TOP)
        button_READ.pack()
        button_WRITE.pack()
        button_DELETE.pack()
        # pack frame_BR
        frame_bRL.pack(side=LEFT)
        frame_bRR.pack(side=RIGHT)
        frame_bLeft.pack(side=LEFT)
        frame_bRight.pack(side=RIGHT)
        frame_bottom.pack(side=TOP)


if __name__ == '__main__':
    client_UI()
