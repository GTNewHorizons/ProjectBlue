include Tools/Common/BackupSettings.make

BACKUP_INCLUDE ?= *
BACKUP_EXCLUDE ?= --exclude build --exclude jars --exclude lib --exclude Releases --exclude Test \
	--exclude Ancillary --exclude game --exclude 'game-*' --exclude logs $(BACKUP_EXCLUDE_EXTRA)

backup: backup_subdir backup_archive $(BACKUP_EXTRA)
	DATE=`date +%Y-%m-%d`; \
	TARFILE=$(BACKUP_DIR)/$(PROJECT)-$$DATE-mc$(MC).tar.gz; \
	tar zcvf $$TARFILE $(BACKUP_EXCLUDE) $(BACKUP_INCLUDE) && \
	ls -l $$TARFILE
	df -H $(BACKUP_VOL)

backup_subdir:
	cd $(BACKUP_VOL); mkdir -p $(BACKUP_SUBDIR)

backup_archive:
	if [ -e Base ]; then \
		mkdir -p Archive; \
		cp -r Base Archive; \
	fi
	if [ -e Tools ]; then \
		mkdir -p Archive; \
		cp -r Tools Archive; \
	fi
	if [ -e mcp ]; then \
		mkdir -p Archive/MCP; \
		cp mcp/Makefile mcp/mctool Archive/MCP; \
	fi
