---
title: Caching
last_updated: 2020-10-03
sidebar: admin_sidebar
toc: true
permalink: caching.html
---


   * `cache/` - various cache directories
     * `agg/`
     * `cdm/`
     * `collection/`
     * `ehcache/`
     * `ncss/`
     * `wcs/`
     
### Can aggregations of many files cause "too many files open" problems?

Union-type aggregations will open all the files in the aggregation at once.
The other types (joinNew, joinExisting) only open one file at a time, and then close it, so these can't cause "too many open file" problems.

If you have "too many open files" errors, and you are not using large Union aggregations, then either there's a file leak (which we would like to know about), or you have your file cache limit set too high relative to your OS file handle limit.

To debug file leaks:

1. Check number of open files with `ulimit -a`.
2. Restart Tomcat to close open files.
3. Monitor open files with `/usr/proc/bin/pfiles [Tomcat Process ID]`
4. Recreate the problem with minimal number of steps so we can reproduce, then send pfiles output to support.

### I have modified my configuration of a `JoinExisting` Aggregation dataset, but nothing has changed.

The files and coordinates in a `JoinExisting` Aggregations are cached, and in some circumstances won't get updated.
The default location for the cache is `${tds.content.root.path}/thredds/cache/agg/` unless you change it in the `threddsConfig.xml` file.
Go to that directory, there will be files with the name of the cached dataset(s).
Delete the file for the dataset that needs updating and restart Tomcat.


### What does the TDS do at startup to read the configuration catalogs? What gets cached? Does it have a way to know a referenced catalog is unchanged? When do referenced catalogs get scanned?

The TDS reads in all the config catalogs at start up.
It caches all of them, and uses the "expires" attribute on the catalog to decide if/when it needs to re-read a catalog.
It must read all `catalogs,` including `catalogRefs`, because it has to know what the possible dataset URLs are, and there is no contract that a client has to read a catalog before accessing the dataset.



## Caching

### We use compressed netCDF files, and the very first access to them are quite slow, although subsequent accesses are much faster, then become slow again after a while.
I can see that TDS uncompress these files to the cdm cache directory, but then they must get deleted.
Is there a way to keep them in the cache permanently?

Essentially this is a tradeoff between storage space and the time to decompress.
We assume you don't want to store the files uncompressed, so you have to pay the price of that.
To control how these files are cached, see CDM library Disk cache.
We would suggest that you use:

~~~xml
<DiskCache>
  <alwaysUse>true</alwaysUse>
  <scour>1 hour</scour>
  <maxSize>10 Gb</maxSize>
</DiskCache>
~~~

and choose `maxSize` carefully.
The trick is to make `maxSize` big enough to keep the _working set_ uncompressed, i.e. if there is a relatively small _hot_ set of files that get accessed a lot, you want to give enough cache space to keep them uncompressed in the cache.
Monitor the cache directory closely to see what files stay uncompressed, and how old they are, and modify `maxSize` as needed.



|`cache.log` |contains log messages related to TDS object and file
caching
