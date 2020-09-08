# Concurrent-Keyword-Counter
A component based console application that concurrently scans/crawls directories and web pages and counts specified keywords.

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
This component recursively scans directories, the user provided, for text corpuses (directories with a prefix "corpus_" which contain text files). <br>
After finding a corpus, it is checked if the <u>Last modified</u> directory attribute has changed since the last scan. <br> 
If that's the case, a new Job is created and submited to the Job queue.<br>
After finishing a scan cycle the component pauses (duration specified in the config file) before starting the next scan.

### Job queue:
Only the directory crawler, CLI and web scanner can write to the Job queue.<br>
Only the job dispatcher component can read the queue.

### Job dispatcher:
This component delegates jobs to the appropriate thread pool component (File/Web scanner).<br>
Jobs are submited as InitiateTaks which pass <b>Future</b> objects to the Result retriever component which can then poll for results<br>
The component is blocked if the job queue is empty.

### Web scanner:
The user initiates a new web scanning job by submiting a website url and jump number using the CLI. <br>
After the dispatcher submits a job to the web scanner , web scanning begins.<br>
Every web job task does the following:
1. Count the specified keywords on the given website
2. If the jump number is greater than 0, start new web scanning jobs for all the links found on the given website (new jobs have a decremented jump number)
Already scanned urls are skipped. After a specified duration (config file) the list of scanned urls is cleared.

### File scanner:
After the dispatcher submits a job to the file scanner (<b>ForkJoinPool), the job is divided into smaller chunks. <br>
<b>RecursiveTasks</b> divide the job, count keywords and finally combine the results.</b>
The job is divided untill the byte limiti (specified in the config file) is satisfied for each task.

### Result retriever:
This component fetches results and does some simple operations with them.<br>
The user communicates with this component via the CLI. <br>
There are two types of requests:
1. Get (blocking command - waits untill results are ready)
2. Query (Returns results if they are ready, otherwise not ready message is returned)
The user can ask for results with the following command: <br>
<b>get file|directory_name</b> - returns results for specified corpus<br>
<b>query web|url or domain</b> - returns results of specific url or summ result for a domain<br>
When fetching web results for a domain, the result retriever initiates tasks for summing the results of all urls with that domain name.<br>
The user can also ask for the results summary:
<b>query file|summary</b><br>
<b>get web|summary</b><br>
Specific tasks for calculating the summary are created. (The summary is stored once it is calculated)

### CLI:

  
