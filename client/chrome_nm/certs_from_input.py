import Tkinter as tk
import re
import json
import parse_40_custom as p4c


def getCertUrls(hashes, all_files):

    manifest_path = p4c.manifest_path
    objs = {}

    global skipall
    skipall = False

    def validateUrl(url):
        regex = re.compile(
            r'^(?:http|ftp)s?://'  # http:// or https://
            r'(?:(?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\.)+(?:[A-Z]{2,6}\.?|[A-Z0-9-]{2,}\.?)|'  # domain...
            r'localhost|'  # localhost...
            r'\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}|'  # ...or ipv4
            r'\[?[A-F0-9]*:[A-F0-9:]+\]?)'  # ...or ipv6
            r'(?::\d+)?'  # optional port
            r'(?:/?|[/?]\S+)$', re.IGNORECASE)
        result = regex.match(url)
        return result

    def handleInput():
        input = text.get("1.0", 'end').strip()
        valid = validate_input(input)
        if valid:
            objs[hashes[i]] = input
            root1.destroy()
        else:
            error_msg.set("Bad input - try again")


    def updateManifest():
        if not p4c.is_filepath_valid(manifest_path):
            return None

        with open(manifest_path) as manifest:
            data = json.load(manifest)

        for i in range(0, len(hashes)):
            try:
                data['files'].append({'cert-url': objs[hashes[i]], 'bin-sha1': hashes[i]})
            except KeyError:
                pass

        with open(manifest_path, 'w') as manifest:
            json.dump(data, manifest)


    def skipFile():
        root1.destroy()

    def skipAll():
        global skipall
        skipall = True
        skipFile()

    def getEntry(event=None):
        handleInput()


    def validate_input(input):
        return False if validateUrl(input)==None else True


    for i in range(0, len(hashes)):
        if skipall:
            break

        root1 = tk.Tk()
        root1.geometry('{}x{}'.format(450, 200))

        error_msg = tk.StringVar()
        hash_v = tk.StringVar()
        name_v = tk.StringVar()

        hash_v.set("Hash: " + str(hashes[i]))
        for entry in all_files:
            if entry['bin-sha1'] == hashes[i]:
                name_v.set("Name: " + str(entry['name']))


        # only works on windows
        # buttonframe = tk.Frame(root1)
        # buttonframe.grid(row=2, column=0, columnspan=2)
        # tk.Button(buttonframe, text="Submit", command=getEntry).grid(row=1, column=0)
        # tk.Button(buttonframe, text="Skip", command=skipFile).grid(row=1, column=1, padx=5, pady=5)

        # enter_button = tk.Button(root1, text="Submit", command=getEntry)
        # skip_button = tk.Button(root1, text="Skip", command=skipFile)
        # skipall_button = tk.Button(root1, text="Skip All", command=skipAll)

        buttonframe = tk.Frame(root1)
        enter_button = tk.Button(buttonframe, text="Submit", command=getEntry)
        skip_button = tk.Button(buttonframe, text="Skip", command=skipFile)
        skipall_button = tk.Button(buttonframe, text="Skip All", command=skipAll)


        label = tk.Label(root1, text="Enter Certificate URL for hash:")
        label_n = tk.Label(root1, textvariable=name_v)
        label_h = tk.Label(root1, textvariable=hash_v)
        label_e = tk.Label(root1, textvariable=error_msg)

        text = tk.Text(root1)
        text.config(width=51, height=5)
        text.bind('<Return>', getEntry)

        label.pack(side=tk.TOP)
        label_n.pack(side=tk.TOP)
        label_h.pack(side=tk.TOP)
        label_e.pack(side=tk.TOP)
        text.pack()

        enter_button.pack(side=tk.LEFT)
        skip_button.pack(side=tk.LEFT)
        skipall_button.pack(side=tk.LEFT)
        buttonframe.pack(side=tk.BOTTOM)
        root1.mainloop()

    updateManifest()

if __name__ == "__main__":
    # for testing
    hashes = ["deadbeef", "deafbeef", "deafbeaf", "deafbaaf"]
    af = [{'bin-sha1':'deadbeef', 'name':'dead beef!'},{'bin-sha1':'deafbeef', 'name':'deaf beef!'},{'bin-sha1':'deafbeaf', 'name':'deaf beaf!'},{'bin-sha1':'deafbaaf', 'name':'deaf baaf!'}]
    print getCertUrls(hashes, af)


