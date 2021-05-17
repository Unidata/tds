---
title: Collection Specification String
last_updated: 2020-08-03
sidebar: tdsTutorial_sidebar
toc: false
permalink: collection_spec_string_ref.html
---

A *collection specification string* creates a collection of files by scanning file directories and looking for matches. 
It can optionally extract a date from a filename. 
It has these parts:

*  A root directory (absolute file path).
* Followed by an optional `/**/` indicating to scan all subdirectories under the root directory.
* Followed by a regular expression that is applied to the filename.
* An optional *date extractor* may be specified that computes a date from the filename.

## Example 1

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/.*nc$
~~~

All files ending with `nc` in the directory `/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km`. 
The `.nc$` is a regular expression which tries to match the path name after the top directory `/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/`. 
The `.` means any number of any character and the `nc$` means "ending with nc". 
If you want to make sure it ends with `.nc`, you need:

~~~
 /data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/.*\.nc$
~~~

Since `.` is a special character in regular expressions, one needs to escape it to match a literal `.`, so `\.nc$` means match the characters `.nc` at the end of the string.

It's generally important to use the `$` to indicate the end of string, since a common convention is to write auxiliary files by naming them `<org file>.<ext>`, and you need to eliminate the auxiliary files from the collection.

## Example 2

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/.*\.nc$
~~~

All files ending with `.nc` in the directory `/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km` and its subdirectories.

## Example 3

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/GFS_Alaska_191km_#yyyyMMdd_HHmm#\.nc$
~~~

Search the directory `/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km` and its subdirectories for files that match the regular expression:

~~~
 GFS_Alaska_191km.........\.nc$
~~~

Remember that an unescaped `.` matches *any* character. An escaped `\.` matches the literal `.` character.

From the filename, extract the date by applying the [SimpleDateFormat](/SimpleDateFormat.html) template `yyyyMMdd_HHmm` to the portion of the filename after `GFS_Alaska_191km`.

## Method For Constructing Collection Specification Strings

The idea is that one copies an example file path, and then modifies it.  
For example, copy an example filename:

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/20090301/GFS_Alaska_191km_20090301_0600.grib1
~~~

Modify it to include subdirectories:

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/GFS_Alaska_191km_20090301_0600.grib1
~~~

Demarcate the part of the filename where the run date is encoded, using `#` chars:

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/GFS_Alaska_191km_#20090301_0600#.grib1
~~~

Substitute a [SimpleDateFormat](/SimpleDateFormat.html):

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/GFS_Alaska_191km_#yyyyMMdd_HHmm#.grib1
~~~

Make sure the name ends with `grib1`:

~~~
/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/GFS_Alaska_191km_#yyyyMMdd_HHmm#\.grib1$
~~~

#### Notes

You have to escape any of these regular expression literal characters that you want to match. 
It's a good idea to avoid these characters in directory and file names, except the `.`

~~~
 .|*?+(){}[]^$\
~~~

* The dot character `.` matches any single character.
* A `^` character matches the null string at the start of a line.
* A `$` character matches the null string at the end of a line.

The *date extractor* can only be used on the filename in a collection specification string. 
If the date is part of a directory name, use the more general `dateFormatMark` on the [`collection`](/grib_feature_collections_ref.html) element.

The *date extractor* element cannot be used after the regular expression. 
So `GFS_Alaska_191km_#yyyyMMdd_HHmm#.*grib$` is ok, but `GFS.*km#yyyyMMdd_HHmm#grib$` is not. 

Use the more general `dateFormatMark`:

~~~
<collection spec="/data/ldm/pub/native/grid/NCEP/GFS/Alaska_191km/**/GFS.*km.*grib$" dateFormatMark="yyyyMMdd_HHmm#.grib#$" />
~~~

### References for regular expressions

* [Wikipedia](https://en.wikipedia.org/wiki/Regular_expression){:target="_blank"}
* [Araxis Merge Regular Expression Reference](https://www.araxis.com/merge/documentation-windows/regular-expression-reference){:target="_blank"}