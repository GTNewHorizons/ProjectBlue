#-------------------------------------------------------------------------------
#
#   Project Blue - Makefile
#
#-------------------------------------------------------------------------------

MODNAME := ProjectBlue
MODTITLE := Project Blue
MODSUBPKG := projectblue

MAJVER := 1
MINVER := 1
BUGVER := 6

PROFILE := Forge-1.7.10-1230
SERVER := game/server

BACKUP_EXCLUDE_EXTRA := --exclude Artwork --exclude Images

include Tools/Mod.make

CPATH := $(CPATH):lib/'*'

backup: backup_rsync

backup_rsync:
	rsync --times -r Artwork Images $(BACKUP_DIR)/$(PROJECT)
