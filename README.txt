###############################################################################
README FOR DAM3ON SYSTEM BY CRITICAL TECHNOLOGIES INC. (CTI)

Last Updated: 03-06-2019

Adam T. Wiethuechter <adam.wiethuechter@critical.com>
Critical Technologies Inc. (CTI)
###############################################################################
###############################################################################
Document Format
===============
Each section of this document is separated by '=' bars above and below its
title and can be taken independently. However it is strongly recommended to do
these sections in the order presented in this document unless otherwise noted.

Subsections are denoted by '=' or '-' markings below the title. The
use of either '=' or '-' is dependent on the author and styling of the
section to make information be easily parsed.

The above markings can span the document on full lines, however are not
required to.

Important notation information and details can be found between '#' marking
lines that extend across the documents 80 character limit.
###############################################################################

===============================================================================
SECTION 0: Introduction
===============================================================================

This is the head node of documents in delivery of the DAM3ON system by
Critical Technologies Inc. (CTI) - specifically the DAM3ON component.

===============================================================================
SECTION 1: Building DAM3ON
===============================================================================

DAM3ON is a highly dynamic system with many inter-dependent components. As such
it is very hard to build blindly using static instructions. The cookbooks
presented in this deliverable are the process that CTI has used to create the
system. However, user configuration and customization can deviate these
instructions to the point where they are merely guides of intended use based on
CTI experience.

One major hurdle in this is that two services primarily use PKCS for secure
communication (mysql and xacml). If you start with building a PDP system you
will be unable to import public keys of PEPs as they have not been created yet.
Likewise if you start with a PEP system you will have no PDP key to import.

It is strongly suggested to have each different system component on
different hosts - and as such the documentation will assume this is the case.

===============================================================================
SECTION 2: Directory Structure
===============================================================================

'db+pdp/' contains files related to the database and Policy Decision Point
(PDP) components. To begin building a DAM3ON system it is suggested you
start here using the cookbook titled 'db+pdp-cookbook.txt'.

'pep/' contains files related to the Policy Enforcement Point (PEP) component.
This should be completed after a system is configured as the 'db+pdp'. The
cookbook titled 'pep-cookbook.txt' guides you through this process.

'client/' contains files and instructions that are required on the client
side of the system. This includes installation for remote attestation.

'utils/' contains various tools created during the development process.
Most of these tools should work as is, however this is not guaranteed.

'git-annex/' contains file pertaining to the git-annex component of the
DAM3ON system.

===============================================================================
SECTION 3: After Building DAM3ON
===============================================================================

After building the system it is strongly suggested that each box be rebooted
and Tomcat instances refreshed to ensure latest configuration files are in
use.

To use the DAM3ON visit the web interface of the PEP server and be sure that
your client is properly configured.

Be sure to run through the cookbooks in the 'client/' folder to properly
set up PGP keys, and the TPM chip on your device.

===============================================================================
SECTION 4: Other Items of Interest
===============================================================================

There are many components to the DAM3ON system, some of which have not been
documented here. This section will call out any major components missed
that we would at least like to point out to the user to explore further.

Policy Administration Point (PAP)
=================================
There is a web interface to help with creating, editing and removing policies
in the DAM3ON system. This can be accessed using DAM3ON by going to the PEP
and requesting access to the resource "pap".

DAM3ON Documentation
====================
There is limited documentation located on the PEP. This can be found by
navigating to your PEP hosts web site '/docs'. WARNING: These documents
are relatively out of date, but could be useful for various things.

Certificates
============
Located on your PEP at the web address '/certs' are various example
certificates for DAM3ON.

Utilities
=========
These are located in 'utils/' and are in a 'as is' state. To run any of the
file perform the following command:

	java -jar <util-file>

===============================================================================
SECTION 5: Using DAM3ON Cheat Sheet
===============================================================================

This section will detail the various ways to use DAM3ON. At lot of this
content can also be found in various other documents here. This is just
a collection of them for easy reference.

Upload / Download Files (w/o git-annex-tahoe)
=============================================
resource: upload_data_mining
action: access / upload

Access Resources
================
This is a list of the various resources available to you by default.

authonly-pgp
	PGP based authentication test

attestonly
	TPM attestation test

pgpauthattest
	TPM attestation and PGP authentication test

pap
	Policy Administration Point

Upload / Download Files (w/ git-annex-tahoe)
============================================
See Section 6 of 'git-annex/git-annex-cookbook.txt'
