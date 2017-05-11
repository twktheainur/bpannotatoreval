# LIRMM: CLEF EHealth 2017 Task 1 Reproduction instructions



[TOC]

## I. Introduction

The LIRMM system uses a dictionary based approach through the SIFR Bioportal Annotator (http://bioportal.lirmm.fr) for the French track and the NCBO Bioportal Annotator (http://bioportal.bioontology.org) for the English track, combined with fallback heuristics. 

Using the SIFR annotator is as simple as sending a request through the HTTP REST API, for example: 

http://services.bioportal.lirmm.fr/annotator/?text=Absence%20de%20tumeur%20maligne&negation=true&apikey=39b6ff93-0c7c-478b-a2d7-1ad098f01a28&ontologies=WHO-ARTFRE

One can also use the UI for direct on-line annotation:

http://bioportal.lirmm.fr/annotator

Bioportal Annotator annotates text with Ontology class URIs. We annotate each line of the corpora with CIM-10 and ICD10 based ontologies and extract the ICD10/CIM-10 codes from the URIs.

The LIRMM system aims at evaluating the Bioportal Annotator concept recognition performance alone, without any disambiguation. As such a higher recall is the main objective, as a post-recognition disambiguation process increases precision by selecting a subset of the recognized concepts. 

**Reference:**

Temporary French publication

### I.1. Team

Two post-doctoral researchers at LIRMM, Montpellier:

* Andon Tchechmedjiev: <andon.tchechmedjiev@lirmm.fr>
* Amine Abdaoui: <amin.abdaoui@lirmm.fr>

An engineer (LIRMM, Montpellier):

* Vincent Emonet <vincent.emonet@lirmm.fr>

Supervised by an Assistant Professor (LIRMM, Monpelier and Stanford Center of Biomedical Informatics Research) in the context of the ANR PractikPharma project around the SIFR BioPortal platform:

* Clément Jonquet <jonquet@lirmm.fr>

### I.2. Methods

1. **FR Run 1 (Aligned and Raw):** Annotation through the SIFR Bioportal Annotator with the CIM-10 French ontology (originating from cismef) combined with a custom built skos vocabulary from the set of dictionaries provided as well as the training corpus (CIM-10DC). The ontology was generated with a heuristic, where labels that correspond to multiple codes are assigned to the most frequent code only. The code distribution was estimated from the training corpus. We wrote a java programme to read the corpora and call the Annotator API in order to annotate the text with ontology classes (for CIM-10, the last component of the URI is the code).
2. **FR Run 2 (Aligned and Raw)**: A fallback strategy starting from the result file of Method 1, and, for each line without any annotations, takes the annotations from a second run, where the custom skos ontology was built without the most frequent code heuristic (higher recall, slightly lower precision). This is, in essence a late fusion technique, that aims at increasing the recall, while keeping the precision very similar.
3. **EN Run 1 (Raw):** Annotation through the SIFR Bioportal Annotator with the a custom built skos vocabulary from the American dictionary provided (ICD10CDC). The ontology was generated with a heuristic, where labels that correspond to multiple codes are assigned to the most frequent code only. The code distribution was estimated from the training corpus. We wrote a java programme to read the corpora and call the Annotator API in order to annotate the text with ontology classes (for ICD10, the last component of the URI is the code).
4. **EN Run 2 (Raw)**: Annotation through the SIFR Bioportal Annotator with the a custom built skos vocabulary from the American dictionary provided (ICD10CDC), combined with an owl version of ICD10 and ICD10CM (extracted from UMLS). The ontology was generated with a heuristic, where labels that correspond to multiple codes are assigned to the most frequent code only. The code distribution was estimated from the training corpus. We wrote a java programme to read the corpora and call the Annotator API in order to annotate the text with ontology classes (for ICD10, the last component of the URI is the code).

### I.3. Contact

The main contact for the reproduction track for the LIRMM system is Andon Tchechmedjiev, you may get in touch with him by email at <andon.tchechmedjiev@lirmm.fr>. 

If you wish may also submit your queries on the issue tracker of the evaluation programme on GitHub:

https://github.com/twktheainur/bpannotatoreval/issues

## II. Prerequisites

### II.1. Hardware

You need a modern machine with at least 4 GB of RAM with 3GB free just for the execution of the evaluation programme and the redis cache server. The programme will run fine on most 2-core systems with a relativly recent CPU (~ 5-6 years), as the CPU requirements are low. 

### II.2. An internet connection

The evaluation programme communicates with the SIFR Bioportal Annotator and NCBO Bioportal Annotator through a REST API and requires an active internet connection of reasonable speed to accomodate the data exchange. 

**Warning:** If your internet connection is behind a proxy that requires an authentication, the program will not work even if you have the appropriate system configuration. The default behaviour of the JDK is to ignore the system properties related to proxy authentication. **If you find yourself in that situation, please get in touch with us, so that we may produce a version of the evaluation program that will work with your proxy settings.**

### II.3. Java 8

The evaluation programme is written in Java and requires that you install the **Java 8 JRE** .

**Make sure that the bin directory of the JRE is in the system PATH variable** (Path on Windows systems).

* **macOS** installation instructions:

  * With the Oracle installer https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-macos.htm

  * With Homebrew: 

    ```shell
     $ brew update
     $ brew tap caskroom/cask
     $ brew install Caskroom/cask/java
    ```

* **Linux** instalation instruction:

  * Official Oracle instructions:

    https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-linux-platforms.htm

  * On Debian:

    https://wiki.debian.org/JavaPackage

  * On Ubuntu:

    https://www.unixmen.com/installing-java-jrejdk-ubuntu-16-04/

  * On Fedora/CentOs: 

    https://fedoraproject.org/wiki/Java

  * On ArchLinux:

    https://wiki.archlinux.org/index.php/java#OpenJDK_8

* **Windows** installation instructions:

  Official oracle dicumentation: https://docs.oracle.com/javase/9/install/installation-jdk-and-jre-microsoft-windows-platforms.htm

### II.4. Redis

The evaluation programme uses a Redis cache server to minimise redundent network activity and to allow resuming the annotation in case of network failure. You need to install redis on you machine:

* On **macOS**, you can use home-brew to install redis. 

  1. First install home-brew if you do not have it already. Open a terminal and run:

     ```shell
     $ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
     ```

  2. Install redis:

     ```shell
     $ brew install redis
     ```

  3. Run redis by opening a terminal and typing:

     ```shell
     $ redis-server
     ```

  Redis will run in the foreground, please leave the window open, unless you want to close redis. You can interrupt the execution with Ctrl + C. Redis will create a database file named dump.rdb, you may delete the file after you have finished with the reprodction. Please use Redis's default port: 6379.

* On **Linux**:

  * Debian or Ubuntu: 

    https://www.digitalocean.com/community/tutorials/how-to-install-and-use-redis

  * Fedora/CentOs:

    http://blog.andolasoft.com/2013/07/how-to-install-and-configure-redis-server-on-centosfedora-server.html

  * ArchLinux:

    https://wiki.archlinux.org/index.php/Redis

  * On other distributions, the instructions are similar to the Debian or Fedora instructions, except for the installation of the dependencies: make sure you have build tools installed (gcc toolchain with automake/autoheader) as well as tcl.


* On **Windows**: 
  * You can use a native Windows fork of redis like so: https://github.com/ServiceStack/redis-windows#running-microsofts-native-port-of-redis
  * Or start the native unix redis using vagrant like so: https://github.com/ServiceStack/redis-windows#running-the-latest-version-of-redis-with-vagrant

## III. Reproducing the results

### III.1. Setting up the evironment

We have prepared an archive with the test corpus and the jar to run the LIRMM system. Please use the corpus from the archive, especially for the French corpus, as we needed to correct erros in lines where the text contains semicolons. The raw text was properly escapes for the Americal dataset but not for the French datasets, thus requireing a painstaking manual correction with the help of regular expressions. 

1. Download the archive and decompress it 

   http://link

2. Open a terminal and go in the decompressed directory. It should be named LIRMMSystemReproduction

3. Make sure you can run java: `java -version` it should run and show you 1.8.X as the version number. 

The corpus directory contains the following corpora:

* corpus/EN/raw/CausesBrutes_EN_test.csv
* corpus/FR/raw/CausesBrutes_FR_test2014.csv
* corpus/FR/aligned/AlignedCauses_2014test.csv

The eval directory contains the aligned and raw scorers:

* Aligned — eval/clefehealth2017Task1_eval.pl_
* Raw — eval/clefehealthTask12017_plainCertifeval.pl

### III.2 Running the experiments

**Remider:** Make sure redis-server is running!

The syntax of the evaluation programme is the following:

```shell
java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator [aligned|raw] /path/to/corpus result.csv cachePrefix annotatorURL apikey [NONE|MFC|CUTOFF|DISAMBIGUATE] [0-1].[0-9]+ ONTOLOGY1 ONTOLOGY2 ... ONTOLOGYN
```

For all the run below, if any network connection occurs or if the server goes down temporarily for some reason, you can simply run the same command again, the annotation will resume from the cache where it had been interrupted. 

#### III.2.1. French Aligned 

------------------

* French Run 1 (aligned):

  ```shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator aligned corpus/FR/aligned/AlignedCauses_2014test.csv FR_aligned_run1.csv FR_aligned_run1 http://services.bioportal.lirmm.fr/annotator 39b6ff93-0c7c-478b-a2d7-1ad098f01a28 NONE 0.0 CIM-10DC-ALLMFC CIM-10
  ```

* French Run 2 (aligned):

  First you need to run the evaluation with another ontology (without filtering heuristics) and then run the fallback program to merge the annotations from run with this new run. 

  1. Run the evaluation:

  ```Shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator aligned corpus/FR/aligned/AlignedCauses_2014test.csv FR_aligned_run_all.csv FR_aligned_run_all http://services.bioportal.lirmm.fr/annotator 39b6ff93-0c7c-478b-a2d7-1ad098f01a28 NONE 0.0 CIM-10DC-ALL CIM-10
  ```

  2. Merge with run 1 through the fallback strategy: 

  ```shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.ClefEHealth2017T1ResultFallbackAligned FR_aligned_run1.csv FR_aligned_run_all.csv FR_aligned_run2.csv
  ```

#### III.2.2. French Raw

------

- French Run 1 (raw):

  ```shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator raw corpus/FR/raw/CausesBrutes_FR_test2014.csv FR_raw_run1.csv FR_aligned_run1 http://services.bioportal.lirmm.fr/annotator 39b6ff93-0c7c-478b-a2d7-1ad098f01a28 NONE 0.0 CIM-10DC-ALLMFC CIM-10
  ```

  Please note that the cache key used is the same as for the aligned evaluation, whih is not an error. Given that there is no difference in the text itself and that our system only used the RawText field, only the number of fields in the output changes between the two tasks. Thus, by using the same cache key, we load all the same annotations from the cache and just write the result in the appropriate format, which takes mere seconds as opposed to more than an hour. 

- French Run 2 (raw):

  First you need to run the evaluation with another ontology (without filtering heuristics during dictionary construction) and then run the fallback program to merge the annotations from run with this new run. 

  1. To run the evaluation:

  ```Shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator raw corpus/FR/raw/CausesBrutes_FR_test2014.csv FR_raw_run_all.csv FR_aligned_run_all http://services.bioportal.lirmm.fr/annotator 39b6ff93-0c7c-478b-a2d7-1ad098f01a28 NONE 0.0 CIM-10DC-ALL CIM-10
  ```

  2. Merge with run 1 through the fallback strategy: 

  ```shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.ClefEHealth2017T1ResultFallbackRaw FR_raw_run1.csv FR_raw_run_all.csv FR_raw_run2.csv
  ```

#### III.2.3. English Raw

----------------------

- English Run 1 (raw):

  ```shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator raw corpus/EN/raw/CausesBrutes_EN_test.csv EN_raw_run1.csv EN_raw_run1 http://services.bioportal.lirmm.fr/ncbo_annotatorplus 9c9d2054-33f0-4d1f-b545-87255257b56c NONE 0.0 ICD10DCD
  ```

- English Run 2 (raw):

  First you need to run the evaluation with another ontology (without filtering heuristics during dictionary construction) and then run the fallback program to merge the annotations from run with this new run. 

  1. To run the evaluation:

  ```Shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.EHealth2017Task1Annotator raw corpus/EN/raw/CausesBrutes_EN_test.csv EN_raw_run_all.csv EN_raw_run_all http://services.bioportal.lirmm.fr/ncbo_annotatorplus 9c9d2054-33f0-4d1f-b545-87255257b56c NONE 0.0 ICD10CDC ICD10CM ICD10
  ```

  2. Merge with run 1 through the fallback strategy:

  ```shell
  java -cp bpeval.jar org.pratikpharma.ehealthtask.task12017.ClefEHealth2017T1ResultFallbackRaw EN_raw_run1.csv EN_raw_run_all.csv EN_raw_run2.csv
  ```

### III.3. Computing result scores

#### III.3.1. French aligned

* Run 1:

  ```Shell
  $ perl ./eval/clefehealth2017Task1eval.pl corpus/FR/aligned/AlignedCauses_2014test.csv FR_aligned_run1.csv 
  ```

* Run 2:

  ```Shell
  $ perl ./eval/clefehealth2017Task1eval.pl corpus/FR/aligned/AlignedCauses_2014test.csv FR_aligned_run2.csv 
  ```

#### III.3.2. French raw

- Run 1:

  ```Shell
  $ perl eval/clefehealthTask12017_plainCertifeval.pl corpus/FR/raw/CausesBrutes_FR_test2014.csv FR_raw_run1.csv 
  ```

- Run 2:

  ```shell
  $ perl eval/clefehealthTask12017_plainCertifeval.pl corpus/FR/raw/CausesBrutes_FR_test2014.csv FR_raw_run2.csv 
  ```

#### III.3.3. English raw

- Run 1:

  ```Shell
  $ perl eval/clefehealthTask12017_plainCertifeval.pl corpus/EN/raw/CausesBrutes_EN_test.csv EN_raw_run1.csv 
  ```

- Run 2:

  ```shell
  $ perl eval/clefehealthTask12017_plainCertifeval.pl corpus/EN/raw/CausesBrutes_EN_test.csv EN_raw_run2.csv 
  ```

#### 

## Appendix 1. Annotator REST API connection information 

### A1.1. French Evaluation

Api-key for the French evaluation: 

```
39b6ff93-0c7c-478b-a2d7-1ad098f01a28
```

Annotator URL for the French evaluation:

http://services.bioportal.lirmm.fr/annotator

Ontology acronyms used in the evaluation:

```
CIM-10
CIM-10DC-ALL
CIM-10DC-ALLMFC
```

### A1.2. English Evaluation

Api-key for the English evaluation: 

```
9c9d2054-33f0-4d1f-b545-87255257b56c
```

Annotator URL for the English evaluation: 

http://services/bioportal/ncbo_annotatorplus

Ontology acronyms used in the evaluation:

```
ICD10
ICD10CM
ICD10CDC
```

## Appendix 2. Building the evaluation programme from source 

The project can be found on GitHub: https://github.com/twktheainur/bpannotatoreval

### A2.1. Requirements 

* You need to have installed Java 1.8 **JDK** 

* You need to install Apache Maven: 

  https://maven.apache.org/install.html

### A2.2. Steps 

1. Clone the repository (you need a github account to do so):

   ```shell
   git clone https://github.com/twktheainur/bpannotatoreval.git
   ```

2. Go into the directory:

   ```shell
   cd bpannotatoreval
   ```

3. Build with maven:

   ```
   mvn clean install assembly:assembly
   ```

The bpeval.jar file will be created in the target directory, it is interchangable with the jar provided in the replication archive. 

