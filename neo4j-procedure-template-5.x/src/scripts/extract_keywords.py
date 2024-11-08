import nltk
import sys
import json
import re
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.probability import FreqDist
import os
import io


def download_nltk_data():

    original_stdout = sys.stdout
    original_stderr = sys.stderr
    sys.stdout = io.StringIO()
    sys.stderr = io.StringIO()

    try:
        nltk.download('punkt', quiet=True)
        nltk.download('stopwords', quiet=True)
    finally:
        sys.stdout = original_stdout
        sys.stderr = original_stderr

download_nltk_data()

def extract_keywords(text):

    text = re.sub(r'\W+', ' ', text)                                        # Remove special characters
    tokens = word_tokenize(text.lower())                                    # Tokenize text
    stop_words = set(stopwords.words('english'))                            # Stop words
    filtered_tokens = [word for word in tokens if word not in stop_words]
    freq_dist = FreqDist(filtered_tokens)                                   # Frequency distribution
    most_common_words = freq_dist.most_common(5)                            # 5 most common words
    keywords = [word for word, freq in most_common_words]                   # Keywords & Frequencies

    return keywords if keywords else []

if __name__ == "__main__":
    input_text = sys.argv[1]
    keywords = extract_keywords(input_text)
    if keywords:
        print(json.dumps(keywords))