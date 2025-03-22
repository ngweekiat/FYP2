import pandas as pd
import re

file_path = r"C:\Users\ngwee\OneDrive - Nanyang Technological University\Documents\SCSE\FYP\Data\Messages.xlsx"
df = pd.read_excel(file_path)

unique_names = df['title'].dropna().unique().tolist()
name_pattern = re.compile(r'\b(' + '|'.join(map(re.escape, unique_names)) + r')\b', re.IGNORECASE)

email_pattern = re.compile(r'\b[\w\.-]+@[\w\.-]+\.\w+\b')
phone_pattern = re.compile(r'(?:(?:\+65[\s\-]?)?\d{4}[\s\-]?\d{4})')

def anonymize_message(message):
    if pd.isna(message):
        return message
    message = name_pattern.sub('[REDACTED_NAME]', message)
    message = email_pattern.sub('[REDACTED_EMAIL]', message)
    message = phone_pattern.sub('[REDACTED_PHONE]', message)
    return message

df['Anonymized_Text'] = df['text'].apply(anonymize_message)

output_path = r"C:\Users\ngwee\OneDrive - Nanyang Technological University\Documents\SCSE\FYP\Data\Messages_Anonymized.xlsx"
df.to_excel(output_path, index=False)

print("Anonymization complete. File saved to:", output_path)
