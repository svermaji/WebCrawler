# WebCrawler

Pure Java code with no framework.

This program as of now checks base url, fetch page and prepare all urls for:
  - href attribute
  - link attribute
  - script attribute

but for downstream to collect other links will only visit page that ends with .html/.htm/.asp/.jsp etc link

Assumptions/Notes:
   - Running PC is not behind firewall/proxy settings - If yes, ensure you are able to access http url by doing needful
   - Java8 will be used
   - Windows machine is used to build the program
   - Attaching log for reference what final output generated
   - Javadoc added for logics, please refer

How to compile:
   - Please execute compile.bat file from root folder of project

How to execute:
   - Please execute run.bat file from root folder of project
   - To run on specific domain either change WebCrawler.java and recompile
   - Or you can pass argument - for this copy command from run.bat and execute on command prompt

With more time
    - Better design can be thought off
    - Performance can be improved
    - Think of adding third party utilities
    - More test can be included
    - Error handling can be improved
    - Ignored url can be treated in better ways
