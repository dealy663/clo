(ns clo.test.nlp.core-nlp
  (:require [clojure.test :refer :all]
            [luminus-migrations.core :as migrations]
            [clo.config :refer [env]]
            [mount.core :as mount]
            [clojure.tools.logging :as log])
  (:import (java.util Properties)
           (edu.stanford.nlp.pipeline StanfordCoreNLP RegexNERAnnotator TokensRegexAnnotator Annotation)
           (java.text SimpleDateFormat)
           (edu.stanford.nlp.ling CoreAnnotations CoreAnnotations$DocDateAnnotation CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TextAnnotation CoreAnnotations$StackedNamedEntityTagAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation CoreAnnotations$LemmaAnnotation
                                  CoreAnnotations$TokensAnnotation)
           (edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$CollapsedDependenciesAnnotation)
           (java.io PrintWriter ByteArrayOutputStream)))
(defn nlp-setup
  [f]
  (mount/start
    #'clo.config/env
    #'clo.db.core/*db*)
  (migrations/migrate ["migrate"] (select-keys env [:database-url]))

  (def props (Properties.))
  (.put props "annotators", "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref")
  (.put props "ner.model", "corenlp/stanford-ner-2015-12-09-2/classifiers/english.all.3class.distsim.crf.ser.gz")
  (def pipeline (StanfordCoreNLP. props))

  ;; These should be explained later in the tutorial
  ;(.addAnnotator pipeline
  ;               (RegexNERAnnotator. "some RegexNer structured file"))
  ;(.addAnnotator pipeline
  ;               (TokensRegexAnnotator. “some tokenRegex structured file”))

  (def formatter (SimpleDateFormat. "yyyy-MM-dd"))
  (def currentTime (.format formatter (System/currentTimeMillis)))
  (def inputText "How do I configure the Named Entity Recognizer in coreNLP? What is the torque spec for a lug nut on a
                  2000 BMW E39? How do I replace the fan on a Macbook Pro A1398?")
  (def document (Annotation. inputText))
  (.set document CoreAnnotations$DocDateAnnotation currentTime)

  ;(def annotation (.annotate pipeline (Annotation. inputText)))
  ;(def stream (ByteArrayOutputStream.))
  ;(.prettyPrint pipeline annotation (PrintWriter. stream))
  (def sentences (.get document CoreAnnotations$SentencesAnnotation))

  (f)

  ;; teardown logic
  )

(use-fixtures
  :once
  nlp-setup
  ;(fn [f]
  ;  (mount/start
  ;    #'clo.config/env
  ;    #'clo.db.core/*db*)
  ;  (migrations/migrate ["migrate"] (select-keys env [:database-url]))
  ;  (f))
  )



(defn show-tokens
  [token]
  (let [text  (.getString token CoreAnnotations$TextAnnotation)
        ner   (.getString token CoreAnnotations$StackedNamedEntityTagAnnotation)
        pos   (.get token CoreAnnotations$PartOfSpeechAnnotation)
        lemma (.get token CoreAnnotations$LemmaAnnotation)]
    (log/debug (str "text=" text "; NER=" ner "; POS=" pos "; LEMMA=" lemma))
    {:text text :ner ner :pos pos :lemma lemma}))

(defn show-edge-info
  [edge]
  (let [dep (.getDependent edge)
        gov (.getGovernor edge)
        rel (.getRelation edge)]
    (log/debug (str "Dependent=" dep))
    (log/debug (str "Governor=" gov))
    (log/debug (str "Relation=" rel))))

(defn get-annotations
  [sentence]
  (doall (map show-tokens (.get sentence CoreAnnotations$TokensAnnotation)))
  (let [dependencies  (.get sentence SemanticGraphCoreAnnotations$CollapsedDependenciesAnnotation)
        first-root    (.getFirstRoot dependencies)]
        (doall (map show-edge-info (.getOutEdgesSorted dependencies first-root)))))


(deftest test-nlp
  (doall (map get-annotations sentences)))