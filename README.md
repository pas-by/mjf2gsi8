# mjf2gsi8
extract Topcon GT-1001 total station RAW data, then output to GSI8 format

Since the 'built-in' export to GSI8 function do not associate with those 'point code',
we wrote the program to cater for it.

# usage
1. download the jdbc sqlite connector, and set it into CLASSPATH.
2. complie with command : javac gtTsRaw.java
3. edit the fields in gtRawConfig.txt
3. run with command : java gtTsRaw
