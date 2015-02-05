# OPENNLP-48

# Coreference in Apache OpenNLP

TODO:

- Describe what's needed for coreference
	- In so far, looks like:
		- Break into sentences
		- Tokenize into a multi dimension array, containing the tokens of each sentence
	- Explain what's a DiscourseEntity and what it carries (gender, number, category, ...)
	- Explain what's a Mention
	- Read more about the Parser, and why it's used here...
	- The example code could be simplied (see Coref01#parse(Parse))
	- Why is a Linker used after the parser?