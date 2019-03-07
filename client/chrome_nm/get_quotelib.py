#!/bin/python


# Author: Jeremy Fields
# Email:  fieldsjd@critical.com
# Date:   9-17-16
# Description: This program is just a helper/wrapper for using tpm_getquote. It takes a 20 byte nonce as its
# first argument, and variable amount of PCR register indices as its 2-N'th argument(s)
import os
import getpass
import base64
import hashlib
from Tkinter import *
# All imports are from python's standard library

# Specify the directory dedicated to this program.
workingDir = "/home/" + getpass.getuser() + "/chrome_nm/tpm/"
tpmSysDir = "/sys/class/tpm/tpm0/device/"

def get_srk_password():
    def stupid(event=None):
        root.destroy()
    root = Tk()
    root.bind('<Return>', stupid)
    app = Frame()
    app.pack()
    root.geometry('{}x{}'.format(275, 90))
    app.master.title("Enter Passphrase")
    password = StringVar() #Password variable
    lbl = StringVar()
    lbl.set("Enter your SRK passphrase")
    Label(app, textvariable=lbl).pack()
    et = Entry(app, textvariable=password, show='*')
    et.focus_set()
    et.pack()

    bt = Button(app, text='Submit', command=root.destroy)
    bt.pack()
    bt.bind('<Return>', stupid)
    app.mainloop()
    return password.get()

# DESC: This is the function that actually does the quote. It takes the nonce and pcrRegs as args
# INPUT: A 20 byte nonce as string, pcrRegs as list()
# OUTPUT: boolean
def getQuote(nonce, pcrRegs):
    nonceFile = open(workingDir + "nonce.txt", "w")
    nonceFile.write(nonce)
    nonceFile.close()

    nvrampass = get_srk_password()

    # add the -a password argument
    command = "tpm_getquote -a " + nvrampass + " " + workingDir + "uuid.txt " + workingDir + "nonce.txt " + workingDir + "tpm_quote.txt"

    for register in pcrRegs:
        command += " " + str(register)
    result = os.system(command)

    if result != 0:
        return False
    return True


def doesUuidExist():
    return os.path.isfile(workingDir + "uuid.txt")


def getUuid():
    try:
        return hashlib.sha256(open(workingDir + "uuid.txt", "rb").read()).hexdigest()
    except:
        return "fail-uuid"

# tpm_quote expects a 20 byte nonce
def isNonceValid(nonce):
    return len(nonce) == 20


def isTpmActiveEnabledOwned():
    owned = open(tpmSysDir+'owned', 'r').read().rstrip()
    active = open(tpmSysDir+'active', 'r').read().rstrip()
    enabled = open(tpmSysDir+'enabled', 'r').read().rstrip()
    if owned == '1' and active == '1' and enabled == '1':
        return True
    return False


def programExist(program):
    def is_exe(fpath):
        return os.path.isfile(fpath) and os.access(fpath, os.X_OK)

    fpath, fname = os.path.split(program)
    if fpath:
        if is_exe(program):
            return True
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            path = path.strip('"')
            exe_file = os.path.join(path, program)
            if is_exe(exe_file):
                return True
    return False

def getPcrVal(index):
    with open(tpmSysDir+"pcrs") as f:
        content = f.readlines()
    content = [x.strip() for x in content]
    return content[index].replace(" ", "")[7:]

def main(nonce, pcrRegs):
    if doesUuidExist() and isNonceValid(nonce) and isTpmActiveEnabledOwned() and programExist("tpm_getquote"):
        qresult = getQuote(nonce, pcrRegs)
        if qresult:
            quote = open(workingDir + "tpm_quote.txt", "r").read()
            quote_b64 = base64.b64encode(quote)
            os.remove(workingDir + "nonce.txt")
            os.remove(workingDir + "tpm_quote.txt")
            return quote_b64
        else:
            return "fail-tpm_quote"
    else:
        return "fail-uuid-nonce-act-en-owned-prog"
