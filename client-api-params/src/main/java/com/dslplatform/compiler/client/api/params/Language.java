package com.dslplatform.compiler.client.api.params;

public enum Language implements Param {
    ACTIONSCRIPT("ActionScript")
  , BACKBONE_JS ("Backbone.js")
  , C           ("C")
  , CLOJURE     ("Clojure")
  , COFFEESCRIPT("CoffeScript")
  , COMMON_LISP ("CommonLisp")
  , CPP         ("C++")
  , C_SHARP     ("C#")
  , D           ("D")
  , ERLANG      ("Erlang")
  , GO          ("Go")
  , HASKELL     ("Haskell")
  , JAVA        ("Java")
  , JAVASCRIPT  ("JavaScript")
  , KNOCKOUT    ("Knockout")
  , KOTLIN      ("Kotlin")
  , LUA         ("Lua")
  , MATLAB      ("MATLAB")
  , MERCURY     ("Mercury")
  , OBJECTIVE_C ("Objective-C")
  , OCAML       ("OCaml")
  , PERL        ("Perl")
  , PHP         ("PHP")
  , PYTHON      ("Python")
  , R           ("R")
  , RUBY        ("Ruby")
  , SCALA       ("Scala")
  , SMALLTALK   ("Smalltalk")
  , VISUAL_BASIC("Visual Basic")
  ;

  public final String language;

  private Language(final String language) {
    this.language = language;
  }

  // -----------------------------------------------------------------------------

  @Override
  public boolean allowMultiple() { return true; }

  @Override
  public void addToPayload(final XMLOut xO) {
    xO.node("language", language);
  }
}
