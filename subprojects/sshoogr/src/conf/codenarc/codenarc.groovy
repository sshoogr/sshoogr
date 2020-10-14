ruleset {
  ruleset('rulesets/basic.xml') {
    exclude 'EmptyCatchBlock'
    exclude 'EmptyMethod'
  }
  ruleset('rulesets/imports.xml') {
    exclude 'MisorderedStaticImports'
    exclude 'NoWildcardImports'
  }
  ruleset('rulesets/naming.xml') {
    exclude 'PropertyName'
    'ClassName' {
      regex = '^[A-Z][a-zA-Z0-9]*$'
    }
    'FieldName' {
      finalRegex = '^_?[a-z][a-zA-Z0-9]*$'
      staticFinalRegex = '^[A-Z][A-Z_0-9]*$'
    }
    'MethodName' {
      regex = '^[a-z][a-zA-Z0-9_]*$'
    }
    'VariableName' {
      finalRegex = '^_?[a-z][a-zA-Z0-9]*$'
    }
  }
  ruleset('rulesets/unused.xml') {}
  ruleset('rulesets/exceptions.xml')
  ruleset('rulesets/logging.xml') {
    exclude 'SystemErrPrint'
    exclude 'SystemOutPrint'
  }
  ruleset('rulesets/braces.xml') {
    exclude 'IfStatementBraces'
  }
  ruleset('rulesets/size.xml') {
    exclude 'CrapMetric'
  }
  ruleset('rulesets/junit.xml')
  ruleset('rulesets/unnecessary.xml')
  ruleset('rulesets/dry.xml')
  ruleset('rulesets/design.xml')
}