# Mockobor

Mocked Observable Observation - library to simplifying some aspects of unit testing with java.




## restrictions

- Java Versions
  - It can't be compiled with java 16+, because of lombok (Compiled with java 8 it runs under java 16 without problems).

- EasyMock
  - If you mock observable with EasyMock and its listener registration methods (add/removeListener) have varargs, then it can be problematic.
  - EayMock does not run with Java 11+
