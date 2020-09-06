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
After finishing a scan cycle the component pauses (duration specified in the config file) before starting the next scan.

### Job queue:
The components directory crawler, CLI and web scanner can currently write to the Job queue.<br>
Jobs are stored as <b>Future</b> objects to enable the Result retriever component to poll for results. <br>
Only the job dispatcher component can read the queue.

### Job dispatcher:
This component delegates jobs to the appropriate thread pool component (File and Web scanner).<br>
The component is blocked if the job queue is empty.

### Web scanner:
The user initiates a new web scanning job by submiting a website url and jump number. <br>
The component then has two jobs:
1. Count the specified keywords on the given website
2. If the jump number is greater than 0, start web scanning jobs for all the links on the given website (new jobs have a decremented jump number)
Already scanned urls are skipped. After a specified duration (config file) the list of scanned urls is cleared.

### File scanner:
