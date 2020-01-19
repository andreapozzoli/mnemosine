MNEMOSYNE
=========
Progetto Distributed System @ Politecnico di Milano - Anno 2019/2020

_Stefano Fedeli - Andrea Pozzoli - Lipei Liu_

**REPLICATED DATA STORE**

Implement a replicated key-value store that offers causal consistency.

*Requirements*
* Implement causal consistency with limited (coordination) overhead. 
* New replicas can be added or removed at runtime.
* The store will be implemented in Java using RMI for the server-side part together with some client code written in Python.

*Assumptions*
* Processes are reliable.
* Channels are point-to-point (no broadcast)
* The same fault model of the Internet (congestions or partition).
* Clients are "sticky": they always interact with the same replica.

## USING
* Go to /deliveries and start the LoadBalancer
```bash
java -jar LoadBalancer.jar
```
* Start as many replicas as you want even on different machines on the same LAN
```bash
java -jar Replica.jar [LoadBalancer IP] [Local machine IP]
```
* Start the as many client as you want even on different machines on the same LAN
```bash
python clientGUI.py
```

### TECNOLOGIES

![alt text](https://tse4.mm.bing.net/th?id=OIP.E_lHuvHs6gwjRdTUNoKUJAAAAA&pid=Api)
![alt text](https://tse1.mm.bing.net/th?id=OIP.GTid8wf7NCbkyOFWWzw4vAAAAA&pid=Api)