# clj-outsidein

A Clojure library for the [Outside.in API](http://developers.outside.in/)

## Usage

The easiest way to use clj-outsidein is to grab it from
[Clojars](http://clojars.org/clj-outsidein).

### Examples


    (use 'outsidein.core
         'resrc.core
         '[clojure.contrib.json :only [read-json]])

    (def root (oi-resource (oi-http-client "key" "secret")))

    (map
     :summary
     (:stories
      (read-json
       (:body (GET (stories-resource root "NY"))))))

    (use '[resrc-client.core :only [?q]])
    (count (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :limit 20))))))

    (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :page 2)))))
    (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :radius 900)))))
    (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :places-only true)))))

    ;; story finder
    (GET (stories-by-uuid-resource
          root
          (:uuid (first (:locations (read-json (:body (GET (locations-resource
                                                            root
                                                            "ithaca,ny")))))))))

    ;; news about the first 5 springfields returned
    (GET (apply stories-by-uuid-resource
                root
                (take 5 (map :uuid (:locations (read-json (:body (GET (locations-resource
                                                                       root
                                                                       "springfield")))))))))


## License

Copyright (C) 2010 Travis Vachon

Distributed under the Eclipse Public License, the same as Clojure.

