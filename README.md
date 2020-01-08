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