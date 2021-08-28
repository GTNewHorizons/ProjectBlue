include Tools/Common/BackupSettings.make

BACKUP_INCLUDE ?= *
BACKUP_EXCLUDE_FILES ?= build game 'game-*' logs jars lib Ancillary Releases Test 

# BACKUP_EXCLUDE ?= --exclude build --exclude jars --exclude lib --exclude Releases --exclude Test \
# 	--exclude Ancillary --exclude game --exclude 'game-*' --exclude logs \
# 	$(patsubst %,--exclude %,$(BACKUP_EXCLUDE_FILES)) $(BACKUP_EXCLUDE_EXTRA)

BACKUP_EXCLUDE ?= $(patsubst %,--exclude %,$(BACKUP_EXCLUDE_FILES)) $(BACKUP_EXCLUDE_EXTRA)

backup: backup_subdir $(BACKUP_EXTRA)
	DATE=`date +%Y-%m-%d`; \
	TARFILE=$(BACKUP_DIR)/Common/$(PROJECT)-$$DATE-Common.tar.gz; \
	tar zcvf $$TARFILE $(BACKUP_EXCLUDE) $(BACKUP_INCLUDE) && \
	ls -l $$TARFILE
	df -H $(BACKUP_VOL)

backup_subdir:
	cd $(BACKUP_VOL); mkdir -p $(BACKUP_SUBDIR)/Common
