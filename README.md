SecSy: Security-oriented Log Synthesis
======================================
Tool for synthesizing business process logs
-------------------------------------------

### About

SecSy is a tool for security-oriented log synthesis. Besides basic synthesis properties (cases per day, office hours, randomized activity duration, ...), its detailed parameter setting for simulating business processes also allows to specify data usage (objects and access modality), actors for process activities and access control constraints for task/object permissions. 

It is also capable of enforcing/violating specific security properties on process traces, such as SoD/BoD, Unauthorized Access, Corrupted log entries, Skipped Activities.

### Library Dependencies

Wolfgang builds upon the following tools and encloses them.

* TOVAL, located at [https://github.com/GerdHolz/TOVAL](https://github.com/GerdHolz/TOVAL "TOVAL: Tom's Java Library")
* JAGAL, located at [https://github.com/iig-uni-freiburg/JAGAL](https://github.com/iig-uni-freiburg/JAGAL "JAGAL: Java Graph Library")
* SEWOL, located at [https://github.com/iig-uni-freiburg/SEWOL](https://github.com/iig-uni-freiburg/SEWOL "SEWOL: Security Workflow Library")
* SEPIA, located at [https://github.com/iig-uni-freiburg/SEPIA](https://github.com/iig-uni-freiburg/SEPIA "SEPIA: Security-oriented PN Framework")

### Documentation

A detailled documentation of SecSy can be found under [http://doku.telematik.uni-freiburg.de/secsy](http://doku.telematik.uni-freiburg.de/secsy "http://doku.telematik.uni-freiburg.de/secsy").
