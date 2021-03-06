# Concurrent-Keyword-Counter
A component based console application that concurrently scans/crawls directories and web pages and counts specified keywords.

## Overview
The system consists of several components that are working concurrently in conjunction. Some components are <b> thread pool </b> based, and other run in their own separate threads.<br><br>
The user provides directories and web pages for scanning.<br>
Web page scans continue (in depth) on all newly found links on the current page.<br>
The user can also get or query (poll) different kinds of results (file corpus, url, domain, summary etc).<br>
The system gracefully handles errors and provides feedback to users.

#### Thread pool based components:
* Web Scanner
* File Scanner
* Result retriever
#### Single thread components:
* Main/CLI (command line interface)
* Job dispatcher
* Directory crawler

There is also a shared <b>blocking queue</b> - Job queue, used for temporarily storing created jobs.
![Alt text](images/image.png?raw=true "")

## Component details:

### Directory crawler:
This component recursively scans directories, that the user has provided, for text corpuses (directories with a prefix "corpus_" which contain text files). <br>
After finding a corpus a new Job is created and submitted to the Job queue.<br>
The last modified value of corpus directories is tracked, so if a directory has been modified, it is scanned again (new jobs are created). <br>
After finishing a scan cycle the component pauses (duration specified in the config file) before starting the next scan.

### Job (blocking) queue:
Only the directory crawler, CLI and web scanner can write to the Job queue.<br>
Only the job dispatcher component can read the queue.

### Job dispatcher:
This component delegates jobs from the job queue to the appropriate thread pool component (File/Web scanner).<br>
Jobs are submitted as InitiateTaks which pass <b>Future</b> objects to the Result retriever component which can then poll for results<br>
The component is blocked if the job queue is empty.

### Web scanner:
The user initiates a new web scanning job by submitting a website url and hop count using the CLI. <br>
After the dispatcher submits a job to the web scanner , web scanning begins.<br>
Every web job task does the following:
1. Count the specified keywords on the given website
2. If the hop count is greater than 0, start new web scanning jobs for all the links found on the given website (new jobs have a decremented hop count)
Already scanned urls are skipped. After a specified duration (config file) the list of scanned urls is cleared.

### File scanner:
After the dispatcher submits a job to the file scanner (<b>ForkJoinPool</b>), the job is divided into smaller chunks. <br>
<b>RecursiveTasks</b> divide the job, count keywords and finally combine the results.</b>
The job is divided until the byte limit (specified in the config file) is satisfied for each task.

### Result retriever:
This component fetches results and is capable of doing some simple operations on them.<br>
The user communicates with this component via the CLI. <br>
There are two types of requests:
1. Get (blocking command - waits until results are ready)
2. Query (Returns results if they are ready, otherwise not ready message is returned)

The user can ask for results with the following commands: <br>
* <b>get file|directory_name</b> - returns results of the specified corpus<br>
* <b>query web|url or domain</b> - returns results (if available) of the specified url or the sum results for the specified domain<br>
(When fetching web results for a domain, the result retriever initiates tasks for summing the results of all urls with that domain name)<br>

The user can also ask for the result summary:
* <b>query file|summary</b><br>
* <b>get web|summary</b><br>
Specific tasks for calculating the summary are created. (The summary is stored once it is calculated)

### CLI:
Supported commands:
* ad directory_path - adds the directory to the list of directories that the crawler component searches for text corpuses (text corpus directories that contain text files must have corpus_ prefix to be found)
* aw url - initiates a web scan for the provided url (hop count is taken from config file)
* get file|corpus_name
* query file|corpus_name
* get web|corpus_url
* query web|corpus_url
* get web|corpus_domain
* query web|corpus_domain
* get file|summary
* query file|summary
* get web|summary
* query web|summary
* cfs - clear file summary
* cws - clear web summary
* stop - exit the application
  
### Configuration file (app.properties):
Parameters are read during app start and cannot be changed during app operation.<br><br>
File structure: <br><br>
keywords=one,two,thre - list of keywords to be counted<br>
file_corpus_prefix=corpus_ - the expected prefix for text corpus directories<br>
dir_crawler_sleep_time=1000 - directoriy crawler pause duration<br>
file_scanning_size_limit=1048576 - limit for file scanner tasks given in bytes<br>
hop_count=2 - number of hops the web scanner does (depth)<br>
url_refresh_time=86400000 - list of visited urls is cleared<br>

## Usage example:

![Alt text](images/example3.png?raw=true "")<br><br><br>

![Alt text](images/example5.png?raw=true "")<br>
![Alt text](images/example4.png?raw=true "")<br>

## Sidenote
This project was an assignment as a part of the course - Concurrent and Distributed Systems during the 8th semester at the Faculty of Computer Science in Belgrade. All system functionalities were defined in the assignment specifications.

## Download
You can download the .jar files [here](download/Concurrent-Keyword-Counter.zip).<br>

## Contributors
- Stefan Ginic - <stefangwars@gmail.com>
