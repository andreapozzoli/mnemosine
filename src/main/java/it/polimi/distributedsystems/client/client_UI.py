# client_UI
from tkinter import *
from clientStick import *

class client_UI:

    def __init__(self):
        # Create an object
        client_obj = clientStick()
        # initialize Tk（），now we get a window
        root = Tk()
        self.createWindow(root)
        self.setTopFrame(root , client_obj)
        self.setBottomFrame(root , client_obj)
        # keep refreshing
        root.mainloop()

    def createWindow(self , root):
        # set the tile of the window
        root.title("client")
        # set the size of the window
        root.geometry('1100x680')
        # set the size of window changable
        root.resizable()

    def setTopFrame(self , root , client_obj):
        # At the top of the window, there is a frame to show the ip_replica and a button for CONNECT
        frame_top = Frame(root)
        frame_topLeft = Frame(frame_top)
        frame_topRight = Frame(frame_top)
        global text_ipReplica  #  for ip_replica needs to be access to more than one function, it should be a global var
        text_ipReplica = Text(frame_topLeft, width = 30, height = 1, font=("Times New Roman", 10))
        label_topLeft = Label(frame_topLeft,text = "IP of your replica: ")
        # add button for CONNECT
        Button(frame_topRight, text="CONNECT", command = lambda: (client_obj.connect_lb(text_ipReplica))).pack()

        # show them
        text_ipReplica.pack(side = RIGHT)
        label_topLeft.pack(side = LEFT)
        frame_topLeft.pack(side = LEFT)
        frame_topRight.pack(side = RIGHT)
        frame_top.pack(side = TOP)

    def setBottomFrame(self , root , client_obj):
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
        label_bLeft.pack(side = TOP)
        label_bRL.pack(side = TOP)
        text_in.pack(side = TOP)
        text_out.pack(side = TOP)
        label_bRR.pack(side = TOP)
        button_READ.pack()
        button_WRITE.pack()
        button_DELETE.pack()
        # pack frame_BR
        frame_bRL.pack(side = LEFT)
        frame_bRR.pack(side = RIGHT)
        frame_bLeft.pack(side = LEFT)
        frame_bRight.pack(side = RIGHT)
        frame_bottom.pack(side = TOP)

if __name__ == '__main__':
    client_UI()
