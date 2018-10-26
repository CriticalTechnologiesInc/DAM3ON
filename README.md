# DAM3ON

## Overview
Please read the slide deck DAM3ON2018.pdf in the media folder first. It is the best, succinct description, and overview of this system 

DAM3ON's attestation feature was built around it's sister project, [SABLE](https://sable.critical.com/). DAM3ON can be utilized without SABLE, but won't be able to perform any attestation related features.

## XACML
In DAM3ON, there are primarily three servers used. This is arbitrary, and everything could be on one, two, or more servers. It's purely for seperation of services, which makes it easier to demonstrate and reason about.

- Server #1
    - MySQL Database
- Server #2
    - Policy Decision Point (PDP)
    - Policy Retrieval Point (PRP) - this is more conceptual than anything
    - Hash Check API (internal API to check TPM attestation data)
- Server #3
    - Policy Enforcement Point (PEP)
    - Policy Administration Point (PAP)
    - Web interface (includes login page, as well as demo resources)

There can be 1..N PEP's, both in XACML in general, as well as DAM3ON. As the name implies, they are enforcement points, which should be put in front of resources you wish to protect. Similarly, you could have 1..N PDPs, and either put them behind a load balancer, or configure individual PEP's to use one optimal for them.

The majority of effort has been around the PDP and PEP. The PRP, is conceptual more than anything, and the PAP is a demonstration resource that allows you to manipulate policies in a web interface.

Noteworthy: In the PDP we added support/modules that allow you to write policies based around attributes such as: IP address, IP range, geo location, State, Email address, Country, and Role, among others.

### Certificates
To allow for attestation of measurments of arbitrary files, you can either hard code "acceptance" of a file into the "approved_file_db", or DAM3ON can be configured to accept a certificate, signed by some authority that you specify that you trust. 

There are four different types of certificates: 
- Proof (aka formal verification of some property)
- Insurance (the assertion of some property is insured $XX amount)
- Pentest (this has some property because it has been pentested)
- None (this has some property because I said so - trust me)

This allows for external entities to make assertions of properties of files, rather than you hard coding entries. This is a feature whos goals is to allow a scenario as follows:

You write a policy that requires a modern, up to date, anti-virus program be on the end users computer. You don't care which one, it just must hold those properties. This breaks end users out of the dictated requirements of specific programs that many other institutions use, in favor of simply meeting some specified properties.

These properties can now be satisfied in several ways:
- you manually enter them in your database as accepted
- you allow any file with those properties, as long as it is insured to some threshold
- you allow your [friend/sister company/consultant] to sign files, without any further proof, as you full trust them
- you allow verified/trusted pentesters to assert they have determined this file holds particular properties
- you allow submission of some formal verification that proves that this file holds some properties, which you then verify.

This is a WIP, but supports most of these. We mainly don't support automated formal verification checking.

### Git/Git-annex/Tahoe
There exists support and -some- documentation on how to utilize Git/Git-Annex whose data is stored in Tahoe. This allows a capability based file storage system to be used in conjunction with DAM3ON. Furthermore, Git-annex allows the storing of only meta-data of git objects, such that potentially sensitive files are never stored on your own server, except briefly upon request. Furthermore, the capability tokens for Tahoe are encrypted, such that your server CAN'T access a file -- only upon an end user requesting access, and then the PDP decrypting the Tahoe token, if a permit decision is given.

## Chrome
The login system was developed for Google Chrome, although it probably works on any Chromium based browser. This section will describe how we communicate with end user systems to perform attestation and authentication, through a browser. This will cover, at a high level, the communcation between a PEP, PDP, and end user.

### Chrome Native Host
```
Native Client enables the execution of native code securely inside web applications through the use of advanced Software Fault Isolation (SFI) techniques. Native Client allows you to harness a client machine’s computational power to a fuller extent than traditional web technologies. It does this by running compiled C and C++ code at near-native speeds, and exposing a CPU’s full capabilities, including SIMD vectors and multiple-core processing with shared memory.
```
We wrote and use a native host to execute on an end users machine, which interacts with the TPM, or smart cards. PGP Authentication is done soley in the browser, and is not a part of the native host.

Included is the source code for this. TPM 1.2 is the only one officially supported, but included is the WIP version for TPM2.0.

The native client for TPM1.2 supports:
- Attestation on linux
- Smartcard on Windows/Linux

### Chrome Extension
Chrome, unsurprisingly, does not allow you to execute arbitrary code directly on an end users machine. One method to communciate, is to use a Chrome Extension that acts as an intermediary between the browser and the native client. We wrote and use a chrome extension, which acts as an intermediary between our login web page, and the native client.

Since we now have our own extension, we added a few usability features as well, such as a list of common resources that will sync across Chrome sessions (if you're logged in).

Included is the source code for the extension. You will have to submit your own version of the extension to the chrome store, because you will have to specify which domains you want it to work on in the extensions manifest file. The included source code is our version, tailored for us (simply in style and which domains it works on), and is merely to act as a template for yours.

### Logging in
After inputting a resource, and selecting an action, once the "Login" button is pressed, this information is sent to the PEP. The PEP the forms an XACML request using this, where the Subject of the request is Anonymous, as we have not yet determined whether this is required. The PEP sends this request to the PDP, where the request is evaluated against any/all policies (depends on policy matching algorithm you select), and a decision is returned.

There are three expected courses of action at this point:
- denied
- permitted
- advice

The first two are obvious, but usually the third is what will occur when requesting access to a protected resource. The "advice" is specified in policy, and advises the PEP/login page what further information is required to be permitted. This could be attestation, PGP authentication, or smart card authentication.

In the case of attestation, or smart card authentication, the login page will then signal the Chrome Extension of such, which will in turn instruct the Native host what to do (perform a TPM quote? Ask user for PIN for smart card?). This information is then passed all the way back up to the PEP, which forms a request, sends to the PDP, receives a decision, and then is either a permit or deny.

## MySQL
Included in the mysql-dbs folder are SQL files containing the structure of all the databases that are required.

## XACML Source
We derived our implementation from the open source project Balana. Our developement environment was Eclipse Oxygen, and included is a full copy of that environment. A description of some projects included:
- balana - base XACML/PDP implementation
- CTIUtils - common functions/libraries
- HashCheck - a Tomcat servlet, internal API that typically runs along side the PDP, which assists in verifying TPM Quotes
- LoginWebInterfacePEP - a Tomcat servlet, the PEP that runs along side the web interface
- PolicyBuilderGUI - this was an attempt to make creating common policies easier. Deprecated since the upgrade to XACML3.0, but shouldn't be too hard to upgrade if someone is interested
- SABLEBackupUtil - a small utility to backup SABLE binary files
- TestSuite - contains a handful of tools we wrote used for debugging and testing during development
- WebPDP - this is the Tomcat servlet that serves as the PDP/API
- XacmlCoreCTI - this is our implementation of a PDP

## Pre-built Package
Included is a pre-built-package folder, which should include all the files necessary to stand up your own DAM3ON instance. Be sure to read the README.s Due to time/money constrains, documentation is unlikely to be fully complete, and is not fully tested.

## XACML3 Examples
Included is a directory which contains some examples showing off some XACML3 Policies

# Overall
This is a large system, and difficult to describe and document fully, especially as a prototype. If you are interested, we'd be happy to help. Feel free to get in contact - fieldsjd@critical.com