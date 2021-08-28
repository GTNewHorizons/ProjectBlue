#-------------------------------------------------------------------------------
#
#   Generic Mod Documentation Makefile
#
#-------------------------------------------------------------------------------

#MODNAME := MyMod
#MODTITLE := My Mod
#MODSUBPKG := mymod

#MAJVER := 1
#MINVER := 0
#BUGVER := 0

MODVER := $(MAJVER).$(MINVER).$(BUGVER)
WEBDIR := web/minecraft/mods/$(MODNAME)
RELDOC := $(MODNAME)-$(MODVER)-Doc.zip
RELDST ?= .

.PHONEY: readme

reldoc: readme
	rm -f $(RELDST)/$(RELDOC)
	cd build; zip -r $(RELDOC) README.html
	mv build/$(RELDOC) $(RELDST)
	if [ -e Doc ]; then zip -r $(RELDST)/$(RELDOC) Doc -x \*.DS_Store; fi

readme: build/README.html

build/%.html: %.html Makefile versions.db
	Tools/Common/genreadme $(MODNAME)

upload:
	cd $(RELDST); rsync --progress --times $(RELDOC) $(COSC):$(WEBDIR)/download
	ssh $(COSC) 'cd $(WEBDIR); unzip -o download/$(RELDOC)'
