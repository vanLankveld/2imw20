# If no arguments are supplied, clear it
if [ $# -eq 0 ]
  then
    clear
fi

# Run tokenizer on file
java -jar ../../libs/beaver/beaver-cc-0.9.11.jar queries.grammar
mv ./parser/TCMQueryParser.java ./parser/TCMQueryParser.java~
mv TCMQueryParser.java ./parser/TCMQueryParser.java