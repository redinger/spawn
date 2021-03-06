(ns leiningen.spawn.genome-util
  "Contains functions for converting genomes into projects"
  (:require [org.bituf.clj-stringtemplate :as st])
  (:import (java.io File)))

(defn write-textfile
  "Writes a string as a textfile. Noop if the file already exists."
  [base name content]
  (let [path (str (.getCanonicalPath base) File/separator name)
        file (File. path)]
    (if (not (.exists file))
      (spit file content))))

(defn write-directory
  "Writes a directory. Noop if the directory already exists.
Returns a java.io.File representing the directory."
  [base name]
  (let [path (str (.getCanonicalPath base) File/separator name)
        file (File. path)]
    (if (not (.exists file))
      (.mkdir file))
    file))

(defn build-filesystem
  "Uses the provided map to create a filesystem structure.
Keys should be strings and represent file/dir names.
String values are saved as text files.
Map values are implied to be dirs and are written recursively."
  ([files]
     (build-filesystem files (File. ".")))
  ([files base]
     (let [base (if (instance? File base) base (File. base))]
       (doseq [name (keys files)]
         (let [content (files name)]
           (cond
            (string? content) (write-textfile base name content)
            (map? content) (build-filesystem content (write-directory base name))
            :else (throw (Exception. "build-filesystem only supports string and map values"))))))))

(defn apply-template
  "Applies a StringTemplate template, and returns the result as a string."
  [template data]
  (let [view (st/get-view-from-classpath template)
        filled (st/fill-view! view data)]
    (st/render-view filled)))


