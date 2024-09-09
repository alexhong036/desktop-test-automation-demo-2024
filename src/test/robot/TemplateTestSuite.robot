*** Settings ***
# TODO: Delete this file when you've created your own test cases
Documentation   Robot test suite files should resist in appropriate folder structures.
Library         TemplateLib     # To make highlighting and outcompletion work run the 'robotLibdoc' task

*** Test Cases ***
Just say hello
    [Tags]  TID: TMPL-001  #Important: Each test should have a Test-ID (TID: *)
    # not just to get all the beniffits of the sdk;)
    Log To Console    Hello QAler

Say hello from library
    [Documentation]
    ...     We can (and should) also provide a test documentation.
    ...     This will be displayed in the execution-log but you can
    ...     also create a Test documentation, just run 'robotTestdoc'
    ...     task and open 'build/doc/testdoc.html'.
    ...
    ...     To test execution, just run 'allTests' task and
    ...     open 'build/robot-reports/<date_time>/log.html'
    [Tags]  TID: TMPL-002
    Tmpl Say Hello From The Library    AEP-Template Library