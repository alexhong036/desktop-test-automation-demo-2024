*** Settings ***
Library    SeleniumLibrary
Library    OperatingSystem
Library    BuiltIn
Library    Collections
Library    CustomPythonLibrary.py
Library    RPA.Desktop
Suite Setup    Set Log Level    TRACE

*** Variables ***
${BROWSER}                 Chrome
${URL}                     https://www.python.org/downloads/
${DOWNLOAD_DIR}            C:/Users/HONG036/Downloads
${PYTHON_INSTALLER}        C:/Users/HONG036/Downloads/python-3.12.5-amd64.exe
${IMAGE_INSTALL_BUTTON}    C:\\Python\\Images\\installnow.png
${IMAGE_CHECKBOX}          C:\\Python\\Images\\checkbox.png
${IMAGE_SUCCESS}           C:\\Python\\Images\\success.png
${IMAGE_CLOSE}             C:\\Python\\Images\\close.png
${IMAGE_PYTHON_LOGO}       C:\\Python\\Images\\pythonforwindows.png
${MAX_RETRIES}             1
${DEFAULT_TIMEOUT}         10
${DEFAULT_CONFIDENCE}      0.8
${DEFAULT_GRAYSCALE}       False

*** Test Cases ***
Verify Python Logo is Visible and Download Python
    [Documentation]    Test to navigate to the Python downloads page, verify the Python logo is visible, download the Python executable, and check if it's correctly downloaded.
    Create Download Directory    ${DOWNLOAD_DIR}
    Open Browser with Custom Preferences    ${URL}    ${BROWSER}
    Maximize Browser Window
    Wait Until Element Is Visible    css=.python-logo    10    "Error! Unable to find Python Logo!"
    ${download_link}=    Get Element Attribute    xpath=//div[@class='header-banner ']//a[contains(@href, '.exe')]    href
    ${file_name}=    Extract File Name from URL    ${download_link}
    Retry Download Until Success    ${download_link}    ${file_name}    ${DOWNLOAD_DIR}
    Open Application    ${PYTHON_INSTALLER}
    Execute Python Installation
    [Teardown]    Close Browser

*** Keywords ***
Execute Python Installation
    Wait Until Image Visible                       ${IMAGE_PYTHON_LOGO}       ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}
    Wait Until Image is Visible and Click Image    ${IMAGE_CHECKBOX}          ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}
    Wait Until Image is Visible and Click Image    ${IMAGE_CHECKBOX}          ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}
    Wait Until Image is Visible and Click Image    ${IMAGE_INSTALL_BUTTON}    ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}
    Wait Until Image Visible                       ${IMAGE_SUCCESS}           60                    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}
    Wait Until Image is Visible and Click Image    ${IMAGE_CLOSE}             ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}

Wait Until Image is Visible and Click Image
    [Arguments]    ${IMAGE_PATH}    ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}
    Click Image Action          ${IMAGE_PATH}    ${DEFAULT_TIMEOUT}    ${DEFAULT_CONFIDENCE}    ${DEFAULT_GRAYSCALE}

Retry Download Until Success
    [Arguments]    ${download_link}    ${file_name}    ${directory}
    ${retry_count}=    Set Variable    0
    FOR    ${i}    IN RANGE    ${MAX_RETRIES}
        Run Keyword And Continue On Failure    Attempt Download    ${download_link}    ${file_name}    ${directory}
        ${retry_count}=    Set Variable    ${i}
        Exit For Loop If    ${retry_count} >= ${MAX_RETRIES}
    END
    Run Keyword If    ${retry_count} >= ${MAX_RETRIES}    Fail    "Download failed after ${MAX_RETRIES} attempts."

Attempt Download
    [Arguments]    ${download_link}    ${file_name}    ${directory}
    Click Element with Visibility Checking    xpath=//div[@class='header-banner ']//a[contains(@href, '.exe')]
    Wait Until File Exists in Download Directory    ${file_name}    ${directory}

Create Download Directory
    [Arguments]    ${directory}
    Create Directory    ${directory}

Open Browser with Custom Preferences
    [Arguments]    ${url}    ${browser}
    ${prefs}=    Create Dictionary    download.default_directory=${DOWNLOAD_DIR}    download.prompt_for_download=False    profile.default_content_settings.popups=0    download.directory_upgrade=True    safebrowsing.enabled=True
    ${options}=    Evaluate    sys.modules['selenium.webdriver'].ChromeOptions()    sys, selenium.webdriver
    Call Method    ${options}    add_experimental_option    prefs    ${prefs}
    Open Browser    ${url}    ${browser}    options=${options}

Click Element with Visibility Checking
    [Arguments]    ${locator}    ${timeout}=30    ${error_message}="Error! Unable to find element!"
    Wait Until Element Is Visible    ${locator}    ${timeout}    ${error_message}
    Click Element    ${locator}

Extract File Name from URL
    [Arguments]    ${url}
    ${file_name}=    Evaluate    os.path.basename('${url}')    os
    Should Match Regexp    ${file_name}    ^python-.*\.exe$
    [Return]    ${file_name}

Wait Until File Exists in Download Directory
    [Arguments]    ${file_name}    ${directory}    ${timeout}=10
    Sleep    3
    ${file_path}=    Join Path    ${directory}    ${file_name}
    Wait Until Keyword Succeeds    ${timeout}    5s    File Should Exist    ${file_path}