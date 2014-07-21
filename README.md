# clj-exif

A Clojure library intended to provide an easier interface to reading and writing
metadata associated with image files. Really just wraps a small part of
[org.apache.commons/commons-imaging](http://commons.apache.org/proper/commons-imaging/)
to achieve this.

Current state is far from thorough, code has only been evaluated with a small
number of JPEG files.

## Usage

See `src/clj-exif/examples.clj` for code examples. In brief:

Read metadata:

    (require '[clj-exif.core :as exif])
    (let [input-file (java.io.File. "/path/to/file.jpg")
          metadata (exif/get-metadata input-file)]
      (println (exif/read metadata)))

    {Root {ExifOffset 194, XResolution 72, ....

Write metadata:

Currently in-place modification is not supported. However, it is possible
to copy an image with an updated metadata set to a new file.

    (require '[clj-exif.core :as exif])
    (import
        '[org.apache.commons.imaging.formats.tiff.constants
           TiffDirectoryConstants
           TiffTagConstants])
    (let [input-file (java.io.File. "/path/to/file.jpg")
          output-file (java.io.File. "/tmp/output.jpg")
          metadata (exif/get-metadata input-file)
          ;; output-set is a writeable copy of the data retrieve from input-file.
          output-set (exif/get-output-set metadata)]
      (exif/update-value output-set
                         TiffDirectoryConstants/DIRECTORY_TYPE_ROOT
                         TiffTagConstants/TIFF_TAG_MAKE
                         ["this is a new value"])
      (exif/copy-file-with-new-metadata input-file output-file output-set))

## TODO

- Don't rely on a snapshot release of commons-imaging.
- Evaluate different input formats and file types.

## License

Copyright © 2014 Oliver McCormack

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
