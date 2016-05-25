# If no arguments are supplied, clear the console.
if [ $# -eq 0 ]
  then
    clear
fi

# Run tokenizer on file
java -jar ../../libs/jflex/jflex-1.6.1.jar -d parser/ queries.flex