# /pdf_to_csv_app.py

import re

import pandas as pd
import pdfplumber
import streamlit as st

st.set_page_config(page_title="PDF to CSV Converter", page_icon="📄", layout="centered")
st.title("📄 PDF Bank Statement to CSV Converter")

def is_transaction_line(text):
    return re.match(r"\d{2} \w{3} \d{2}", text.strip()) is not None

def extract_transactions(pdf_file):
    transactions = []
    with pdfplumber.open(pdf_file) as pdf:
        for page in pdf.pages:
            lines = page.extract_text().split('\n')
            for line in lines:
                if is_transaction_line(line):
                    parts = re.split(r'\s{2,}', line.strip())
                    if len(parts) >= 3:
                        date = parts[0]
                        details = ' '.join(parts[1:-2])
                        paid_out = parts[-2] if re.match(r"^\d+(\.\d+)?$", parts[-2]) else ""
                        paid_in = parts[-1] if re.match(r"^\d+(\.\d+)?$", parts[-1]) else ""
                        transactions.append([date, details, paid_out, paid_in])
    return transactions

uploaded_file = st.file_uploader("Upload your Bank Statement PDF", type=["pdf"])

if uploaded_file:
    st.success("File uploaded. Processing...")
    data = extract_transactions(uploaded_file)
    df = pd.DataFrame(data, columns=["Date", "Details", "Paid Out", "Paid In"])

    st.dataframe(df, use_container_width=True)

    csv_bytes = df.to_csv(index=False).encode('utf-8')
    st.download_button(
        label="⬇️ Download CSV",
        data=csv_bytes,
        file_name="statement.csv",
        mime="text/csv"
    )