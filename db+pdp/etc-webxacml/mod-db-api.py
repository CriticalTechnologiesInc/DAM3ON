import MySQLdb
from flask import Flask, request

import json
app = Flask(__name__)

# Simple and fast way to validate a SHA1 hash. Avoids expensive regex
# 1: is it 40 chars?
# 2: can hex be converted to int? (aka valid hex)
# http://stackoverflow.com/questions/32234169/sha1-string-regex-for-python
def is_sha1(maybe_sha):
    if len(maybe_sha) != 40:
        return False
    try:
        int(maybe_sha, 16)
    except ValueError:
        return False
    return True


def hashesThatDbDoesHave(clientHashes):
    # before anything, validate all the hashes
    for hash in clientHashes:
        if not(is_sha1(hash)):
            return False


    # connect to DB
    db = MySQLdb.connect(host="localhost", port=4655, user="file_user", passwd="CriticalAardvark#25", db="approved_file_db")
    cursor = db.cursor()

    # needed to format python list --> sql list
    format_strings = ','.join(['%s'] * len(clientHashes))
    # execute query
    cursor.execute("SELECT binSha1 FROM files WHERE binSha1 in (%s)" % format_strings, tuple(clientHashes))

    # get results
    modfiles = [result[0] for result in list(cursor.fetchall())]
    # close connection
    cursor.close()
    db.close()
    # return the list of hashes that the database DOES have
    return modfiles


@app.route("/hashcheck", methods=['POST'])
def api():
    try:
        clientHashes = json.loads(request.form['hashes'])
        result = hashesThatDbDoesHave(clientHashes)
    except:
        return "Bad Request", 400

    if result:
        return json.dumps(result), 200
    else:
        return "[]", 200


if __name__ == "__main__":
    #app.run(debug=True) # local testing
    app.run(host="0.0.0.0", port=4653)  # production

