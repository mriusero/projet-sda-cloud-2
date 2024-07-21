import nltk
import sys
import json
import re
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.probability import FreqDist
import os
import io

# Fonction pour télécharger les données nltk sans afficher les messages
def download_nltk_data():
    # Redirection des sorties standard et d'erreur
    original_stdout = sys.stdout
    original_stderr = sys.stderr
    sys.stdout = io.StringIO()
    sys.stderr = io.StringIO()

    try:
        nltk.download('punkt', quiet=True)
        nltk.download('stopwords', quiet=True)
    finally:
        # Restaurer les sorties originales
        sys.stdout = original_stdout
        sys.stderr = original_stderr

# Télécharger les données nltk silencieusement
download_nltk_data()

def extract_keywords(text):
    # Nettoyer le texte
    text = re.sub(r'\W+', ' ', text)
    # Tokenisation
    tokens = word_tokenize(text.lower())
    # Enlever les mots vides (stop words)
    stop_words = set(stopwords.words('english'))
    filtered_tokens = [word for word in tokens if word not in stop_words]
    # Calculer la fréquence des mots
    freq_dist = FreqDist(filtered_tokens)
    # Obtenir les 10 mots les plus fréquents
    most_common_words = freq_dist.most_common(5)
    # Extraire les mots (en excluant les fréquences)
    keywords = [word for word, freq in most_common_words]
    return keywords if keywords else []

if __name__ == "__main__":
    input_text = sys.argv[1]
    keywords = extract_keywords(input_text)
    if keywords:
        print(json.dumps(keywords))