#!/usr/bin/python2.7
import ConfigParser
import sys
import email
import re

import calendar
import dateparser
import xmltodict as xmltodict
import datetime
import pyttsx
from BeautifulSoup import BeautifulSoup

############# VARS ##############
top_list = "ctidev3.critical.com" # top level domain
speech_engine = "pico" # espeak or pico
#################################

# config = ConfigParser.RawConfigParser()
# config.read('/home/pi/sandbox/config.properties')
# #message = open('email.txt', 'r').readlines()
# log = open(config.get('Paths','logLocation'), 'a+')

with open("/etc/webxacml/config.xml") as f:
    content = f.read()
config = BeautifulSoup(content)
log = open(config.basic.log.contents[0], 'a+')


formatted_msg = []

# this def uses 'espeak' which sucks
def alert(cap):
    engine = pyttsx.init()
    engine.setProperty("rate",160)
    engine.setProperty("voice", "english-us")
    if cap['alert']['status'] == "Actual":
        engine.say("This is an actual alert.")
    else:
        engine.say("This is just a test alert.")

    engine.say("Alert from: " + cap['alert']['info']['senderName'])

    curDate = dateparser.parse(cap['alert']['sent'])
    engine.say("Sent on: " + curDate.strftime('%B %d %Y at %H:%M') + "GMT")

    engine.say("Severity is: " + cap['alert']['info']['severity'])
    engine.say("Urgency is: " + cap['alert']['info']['urgency'])
    engine.say("The subject is: " + cap['alert']['info']['headline'])
    engine.say("The message is: " + cap['alert']['info']['description'])

    engine.runAndWait()

# this def uses 'pico' (android tts) which is marginally better
def alert_t(cap):
    import talkey
    engine = talkey.Talkey(engine_preference=['pico'])
    if cap['alert']['status'] == "Actual":
        engine.say("This is an actual alert.")
    else:
        engine.say("This is just a test alert.")

    engine.say("Alert from: " + cap['alert']['info']['senderName'])

    curDate = dateparser.parse(cap['alert']['sent'])
    engine.say("Sent on: " + curDate.strftime('%B %d %Y at %H:%M') + "GMT")

    engine.say("Severity is: " + cap['alert']['info']['severity'])
    engine.say("Urgency is: " + cap['alert']['info']['urgency'])
    engine.say("The subject is: " + cap['alert']['info']['headline'])
    engine.say("The message is: " + cap['alert']['info']['description'])


if __name__ == '__main__':

    message = sys.stdin.read()

    em_data = ''.join(message)

    email_message = email.message_from_string(em_data)
    # Convert raw email into email message data type

    from_field = re.search(r'[\w\.-]+@[\w\.-]+', email_message['From']).group(0)
    # Find who the email is from

    from_user, from_domain = from_field.split("@")

    to_field = re.search(r'[\w\.-]+@[\w\.-]+', email_message['To']).group(0)
    # Find who email is going to

    subject_field = email_message['Subject']

    log.write("INFO: From: " + from_field + " To: " + to_field + " Subject: " + subject_field + "\n")
    if top_list in from_domain:
        log.write("INFO: Message was from " + top_list + ", so broadcasting message now.\n")


        if not email_message.is_multipart():
            CAP = email_message.get_payload()
        else:
            for pl in email_message.get_payload():
                print pl.get_content_type()
                print str(pl.get('Content-Disposition'))
                if (pl.get_content_type() == 'text/plain') and (pl.get('Content-Disposition') is None):
                    CAP = pl.get_payload()

        CAP_dict = xmltodict.parse(CAP)

        if speech_engine == "espeak":
            alert(CAP_dict)
        elif speech_engine == "pico":
            alert_t(CAP_dict)

    else:
        log.write("INFO: Message was not from top list, ignoring message.\n")

    log.close()
