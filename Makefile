# Makefile for humla lectures

help:
	@echo "make <target>"
	@echo "http	starts the http server listening on tcp/9000."
	@echo "clean	cleans all temporary directories."
	@echo "pdf	creates a pdf document for every lecture file."
	@echo "toc	creates a table of contents in JSON for all lecture files." 
	@echo "all	cleans everything and creates all pdf files and toc." 
	@echo ""

http:
	humla/bin/http-server.sh

clean:
	rm -fr cache
	rm -fr pdf
	rm -f toc.json

pdf:
	humla/bin/generate-pdfs.sh	

toc:
	humla/bin/generate-toc.sh

all:	clean pdf toc
