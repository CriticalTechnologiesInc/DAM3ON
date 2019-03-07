#!/usr/bin/env python
from PyKCS11 import *
import OpenSSL
import json
import binascii
import platform
import sys
import ctypes
import time
import urllib2
import Tkinter as tk
import gc

if platform.system() == "Windows":
    os.environ["PYKCS11LIB"] = "c:\\windows\\system32\\opensc-pkcs11.dll"
else:
    os.environ["PYKCS11LIB"] = "/usr/lib/opensc-pkcs11.so"


def zerome(string):
    location = id(string) + 20
    size = sys.getsizeof(string) - 20
    del string
    if platform.system() == "Windows":
        memset = ctypes.cdll.msvcrt.memset
        memset(location, 0, size)
    gc.collect()


def getCert(id, session):
    tmp = session.findObjects([(PyKCS11.CKA_ID, id), (PyKCS11.CKA_CLASS, PyKCS11.CKO_CERTIFICATE)])
    if len(tmp) < 0:
        raise ValueError("No certificate found for id: " + str(id))
    cert = tmp[0]

    all_attributes = [PyKCS11.CKA_SUBJECT, PyKCS11.CKA_VALUE, PyKCS11.CKA_ISSUER, PyKCS11.CKA_CERTIFICATE_CATEGORY,
                      PyKCS11.CKA_END_DATE]

    attributes = session.getAttributeValue(cert, all_attributes)
    attrDict = dict(list(zip(all_attributes, attributes)))

    x509 = OpenSSL.crypto.load_certificate(OpenSSL.crypto.FILETYPE_ASN1,
                                                       str(bytearray(attrDict[PyKCS11.CKA_VALUE])))
    return OpenSSL.crypto.dump_certificate(OpenSSL.crypto.FILETYPE_PEM, x509)


def getKey(keys):
    acceptedValues = {}
    for key in keys:
        d = key.to_dict()
        if d["CKA_SIGN"]:
            acceptedValues[d["CKA_LABEL"]] = d["CKA_ID"]
    if len(acceptedValues) < 1:
        popup("Error", "Couldn't find any keys to use")
        return None
    else:
        return getKeyIdGUI(acceptedValues)


def init():

    pkcs11 = PyKCS11Lib()
    pkcs11.load()  # define environment variable PYKCS11LIB=YourPKCS11Lib

    # not dealing with multiple card readers right now. Default to the zeroth one.
    # seems like it would be uncommon to have multiple readers anyways
    slot = pkcs11.getSlotList()[0]

    # start the session object
    return pkcs11.openSession(slot, PyKCS11.CKF_SERIAL_SESSION | PyKCS11.CKF_RW_SESSION)


def getKeyIdGUI(keys):
    root = tk.Tk()
    root.lift()

    h = len(keys) * 24 + 80  # default 80px + 24px for each key
    center(root, (250, h))
    root.wm_title("Choose Key")
    if platform.system() == "Windows":
        root.iconbitmap('favicon.ico')
    root.protocol("WM_DELETE_WINDOW", lambda: quit(1))

    label = tk.Label(root, text="Please choose a key to use:")
    label.pack(side=tk.TOP)

    v = tk.IntVar()
    group = tk.Frame(root, padx=50, pady=5)
    counter = 0
    for key in keys:
        if counter == 0:
            tmp = tk.Radiobutton(group, text=key, variable=v, value=keys[key])
            tmp.select()
            tmp.pack(anchor=tk.W)
        else:
            tk.Radiobutton(group, text=key, variable=v, value=keys[key]).pack(anchor=tk.W)
        counter += 1
    group.pack(padx=2, pady=5, anchor=tk.W)

    buttonframe = tk.Frame(root)
    submit_button = tk.Button(buttonframe, text="Submit", command=root.destroy)
    submit_button.pack(side=tk.LEFT)
    exit_button = tk.Button(buttonframe, text="Exit", command=root.destroy)
    exit_button.pack(side=tk.LEFT)
    buttonframe.pack(side=tk.BOTTOM)

    root.mainloop()
    return v.get()


def center(toplevel, desired_size):
    toplevel.update_idletasks()
    w = toplevel.winfo_screenwidth()
    h = toplevel.winfo_screenheight()
    size = tuple(int(_) for _ in toplevel.geometry().split('+')[0].split('x'))
    x = w/2 - size[0]/2
    y = h/2 - size[1]/2
    toplevel.geometry("%dx%d+%d+%d" % (desired_size + (x, y)))


class gui_login:

    def handleInput(self, event=None):
        input = self.text.get().strip()
        try:
            self.session.login(input)
            zerome(input)
            del input
            self.root.destroy()
        except Exception as e:
            self.error_msg.set(e)
            self.text.delete(0, 'end')

    def run(self, session):
        self.session = session
        self.root = tk.Tk()
        self.root.attributes("-topmost", True)
        self.root.focus_set()
        self.root.focus_force()
        self.root.after(1, lambda: self.root.focus_force())
        center(self.root, (250,100))
        self.root.wm_title("PIN Entry")
        if platform.system() == "Windows":
            self.root.iconbitmap('favicon.ico')
        self.root.protocol("WM_DELETE_WINDOW", lambda: quit(1))

        self.error_msg = tk.StringVar()

        buttonframe = tk.Frame(self.root)

        label = tk.Label(self.root, text="Please enter your PIN:")
        label_error = tk.Label(self.root, textvariable=self.error_msg)

        self.text = tk.Entry(self.root, show="*", width=16)
        self.text.bind('<Return>', self.handleInput)

        label.pack(side=tk.TOP)
        label_error.pack(side=tk.TOP)

        self.text.pack()
        self.text.focus()

        submit_button = tk.Button(buttonframe, text="Submit", command=self.handleInput)
        submit_button.pack(side=tk.LEFT)

        exit_button = tk.Button(buttonframe, text="Exit", command=lambda: quit(1))
        exit_button.pack(side=tk.LEFT)

        buttonframe.pack(side=tk.BOTTOM)
        self.root.mainloop()


def popup(message):
    root = tk.Tk()
    root.title("Alert")

    root.lift()
    root.attributes("-topmost", True)
    root.focus_set()
    root.focus_force()
    root.after(1, lambda: root.focus_force())

    width = len(message) * 8

    if width < 250:
        width = 250

    if width > 900:
        width = 900


    center(root, (width, 150))
    root.wm_title("Alert")
    if platform.system() == "Windows":
        try:
            root.iconbitmap('favicon.ico')
        except:
            pass
    root.protocol("WM_DELETE_WINDOW", lambda: quit(1))

    label = tk.Label(root, text=message)
    label.pack(side="top", fill="both", expand=True, padx=20, pady=20)

    ok_button = tk.Button(root, text="OK", command=lambda: root.destroy(), height=2, width=3)
    ok_button.pack(side="left", fill="x", expand=True, pady=10, padx=10)

    cancel_button = tk.Button(root, text="Cancel", command=lambda: quit(1), height=2, width=3)
    cancel_button.pack(side="right", fill="x", expand=True, pady=10, padx=10)

    root.mainloop()
    return True

def getNonce(email):
    try:
        j = json.loads(urllib2.urlopen("https://ctidev4.critical.com/api/nonce.php?email="+email).read())
        return str(j["nonce"])
    except:
        return None

def quit_custom():
    t = []
    t["status"] = "fail"
    return json.dumps(t)

def getSignature():
    waiting = True
    while waiting:
        try:
            session = init()
            waiting = False
        except PyKCS11.PyKCS11Error:
            if popup("Please insert your PIV card and press OK"):
                time.sleep(1)
                pass
            else:
                return quit_custom()
        except IndexError:
            if popup("Please connect a card reader and press OK"):
                time.sleep(1)
                pass
            else:
                return quit_custom()


    # login, as we need to read available private keys + sign
    gui_login().run(session)

    # find private key to use
    privKeys = session.findObjects([(PyKCS11.CKA_SIGN, True)])
    keyID = (getKey(privKeys),)  # yes, that trailing comma is needed. no idea why

    # Get the privKey with the ID specified by the user
    privKey = session.findObjects([(PyKCS11.CKA_CLASS, PyKCS11.CKO_PRIVATE_KEY), (PyKCS11.CKA_ID, keyID)])[0]

    d = {}
    c = getCert(keyID, session)
    d["cert"] = c

    tmp_crt = OpenSSL.crypto.load_certificate(OpenSSL.crypto.FILETYPE_PEM, c)
    email = tmp_crt.get_subject().emailAddress

    if email is None:
        d['status'] = "fail"
        return json.dumps(d)

    nonce = getNonce(email)

    # Sign the data using sha256
    signature = session.sign(privKey, nonce, Mechanism(PyKCS11.CKM_SHA256_RSA_PKCS, None))

    d["signature"] = binascii.hexlify(bytearray(signature))
    d["type"] = "scard"
    d["email"] = email
    d["status"] = "success"

    # logout
    session.logout()
    session.closeSession()

    ds = json.dumps(d)
    return ds

if __name__ == "__main__":
    res = getSignature()

    import getpass
    if platform.system() is "Windows":
        f = open("c:\\users\\" + getpass.getuser() + "\\desktop\\sig.json", 'w')
    else:
        f = open("/home/" + getpass.getuser() + "/sig.json", 'w')
    f.write(res)
    f.close()