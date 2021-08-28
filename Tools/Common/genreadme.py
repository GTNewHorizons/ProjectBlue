#----------------------------------------------------------------------------------------
#
#   genreadme - Generate README.html file from template and version database
#
#   Usage: genreadme mod_name
#
#----------------------------------------------------------------------------------------

import os, sys, sqlite3
from jinja2 import Template
from collections import namedtuple
from BeautifulSoup import BeautifulSoup

web_root = "http://www.cosc.canterbury.ac.nz/greg.ewing/minecraft/mods/SGCraft"

class Row(object):

    def __init__(self, cursor, row):
        d = self.__dict__
        for idx, col in enumerate(cursor.description):
            d[col[0]] = row[idx]
    
    def __str__(self):
        return str(self.__dict__)

def jinjafy(html_in):
    soup = BeautifulSoup(html_in)
    for node in soup.findAll(True, "jinja2"):
        text = " ".join(node.findAll(text = True))
        #print "jinjafy: found", text
        node.replaceWith(text)
    return str(soup)

def href(text, url):
    return '<a href="%s">%s</a>' % (url, text)

def download_href(filename):
    return href(filename, "%s/download/%s" % (web_root, filename))

def main():
    args = sys.argv[1:]
    mod_name = args[0]
    conn = sqlite3.connect("versions.db")
    conn.row_factory = Row
    curs = conn.cursor()
    rows = curs.execute("""select mc_version, mod_version, forge_version, forge_link, ic2_version,
        cc_version, oc_version from version""")
    data = rows.fetchall()
    for row in data:
        #print row
        row.jar_href = download_href("%s-%s-mc%s.jar" % (mod_name, row.mod_version, row.mc_version))
        row.doc_href = download_href("%s-%s-Doc.zip" % (mod_name, row.mod_version))
        row.src_href = download_href("%s-%s-mc%s-Source.zip" % (mod_name, row.mod_version, row.mc_version))
        row.forge_href = href(row.forge_version, row.forge_link)
    html_in = open("README.html").read()
    html_jinja = jinjafy(html_in)
    open("template.html", "w").write(html_jinja)
    tmpl = Template(html_jinja)
    html_out = tmpl.render(versions = data)
    open("build/README.html", "w").write(html_out)

main()
