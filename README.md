Accompanying source code for the benchmark of the paper "A Context-Free Symbiosis of Runtime Verification & Automata Learning".


#### Prerequisites 

* A working JDK (8+) installation
* A working Maven installation


#### Running the benchmark

* Run `mvn clean package`.
* In the `target`directory you will find a `havelund65-benchmark.jar` which can be executed with `java -jar path/to/jar`
  * Once started, the benchmark will create two files (`output.csv`, `output.log`) in the directory from which you started the benchmark.
  * The benchmarks are run in parallel. Depending on how many cores your system has, the process may require up to 8GB of RAM.