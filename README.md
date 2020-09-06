# Concurrent-Keyword-Counter
A console application that concurrently scans/crawls directories and web pages and counts specified keywords.

## Overview
The system consists of several components working concurrently in conjunction. Some components are <b> thread pool </b> based, and other run in their own separate threads.

#### Thread pool based components:
* Web Scanner
* File Scanner
* Result retriever
#### Single thread components:
* Main/CLI (command line interface)
* Job dispatcher
* Directory crawler

There is also a shared <b>blocking queue</b> - Job queue, used for assigning and starting jobs.
![Alt text](images/image.png?raw=true "")

## Component details:

### Directory crawler:
This component recursively scans specified directories for text corpuses (directories with a prefix "corpus_" which contain text files). <br>
After finding a corpus, it is checked if the <u>Last modified</u> directory attribute has changed since the last scan. <br> 
If that's the case, a new Job is created and submited to the Job queue.<br>
After finishing a scan cycle, the component pauses before the next scan for a specified (configuration file) amount of time.

### Job queue:
