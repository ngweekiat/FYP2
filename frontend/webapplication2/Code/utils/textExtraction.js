import Tesseract from "tesseract.js";
import MsgReader from "@kenjiuno/msgreader";
import JSZip from "jszip";
import { parseStringPromise } from "xml2js";
import * as XLSX from "xlsx";


/**
 * Parses an .msg Outlook email file and extracts its body & attachments.
 * @param {File} msgFile - The .msg file to parse.
 * @returns {Promise<object>} - Parsed email content with extracted attachments.
 */
export const parseMsgEmail = async (msgFile) => {
    try {
        const msgBuffer = await msgFile.arrayBuffer();
        const reader = new MsgReader(msgBuffer);
        const msgData = reader.getFileData();

        // Initialize email content object
        const emailContent = {
            subject: msgData.subject || "No Subject",
            sender: msgData.sender || "Unknown Sender",
            body: msgData.body || "No Email Body",
            dates: {
                date: msgData.date || "Unknown Date",
                receivedDate: msgData.receivedDate || "Unknown Date",
                sentDate: msgData.sentDate || "Unknown Date",
                messageDate: msgData.messageDate || "Unknown Date",
                deliveryTime: msgData.deliveryTime || "Unknown Date",
                creationTime: msgData.creationTime || "Unknown Date",
            },
            attachments: [],
        };

        // Process attachments if they exist
        if (msgData.attachments && msgData.attachments.length > 0) {
            for (const attachment of msgData.attachments) {
                const { fileName, content } = attachment;

                if (!fileName || !content) continue; // Skip invalid attachments

                // Detect file type from filename
                const fileType = fileName.split(".").pop().toLowerCase();

                let extractedText = null;

                try {
                    if (["jpg", "jpeg", "png"].includes(fileType)) {
                        // Convert Base64 to Blob and pass to OCR function
                        const blob = base64ToBlob(content, "image/" + fileType);
                        extractedText = await extractTextFromImage(blob);
                    } else if (["pptx"].includes(fileType)) {
                        const blob = base64ToBlob(content, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
                        extractedText = await extractTextFromPptx(blob);
                    } else if (["xlsx", "xls", "csv"].includes(fileType)) {
                        const blob = base64ToBlob(content, "application/vnd.ms-excel");
                        extractedText = await extractTextFromExcel(blob);
                    } else {
                        extractedText = "Unsupported file type.";
                    }
                } catch (error) {
                    console.error(`Error extracting text from attachment ${fileName}:`, error);
                    extractedText = "Error extracting text.";
                }

                // Store attachment details
                emailContent.attachments.push({
                    name: fileName,
                    extractedText,
                });
            }
        }

        return emailContent;
    } catch (error) {
        console.error("Error parsing .msg file:", error);
        return { error: "Failed to parse .msg file." };
    }
};

/**
 * Converts Base64 content to a Blob.
 * @param {string} base64Data - The Base64 encoded string.
 * @param {string} mimeType - The MIME type of the file.
 * @returns {Blob} - The converted Blob.
 */
const base64ToBlob = (base64Data, mimeType) => {
    const byteCharacters = atob(base64Data);
    const byteArrays = [];

    for (let i = 0; i < byteCharacters.length; i += 512) {
        const slice = byteCharacters.slice(i, i + 512);
        const byteNumbers = new Array(slice.length);
        for (let j = 0; j < slice.length; j++) {
            byteNumbers[j] = slice.charCodeAt(j);
        }
        byteArrays.push(new Uint8Array(byteNumbers));
    }

    return new Blob(byteArrays, { type: mimeType });
};

  
  
/**
 * Extract text from an image using OCR.
 * @param {File} image - The image file to process.
 * @returns {Promise<string>} - The extracted text.
 */
export const extractTextFromImage = async (image) => {
  try {
    const { data } = await Tesseract.recognize(image, "eng", {
      logger: (m) => console.log(m),
    });
    return data.text; // Extracted text
  } catch (error) {
    console.error("OCR extraction failed:", error);
    return "Error extracting text from image.";
  }
};

/**
 * Extracts text from a PowerPoint .pptx file.
 * @param {File} file - The PowerPoint file to process.
 * @returns {Promise<string>} - Extracted text.
 */
export const extractTextFromPptx = async (file) => {
    try {
        const arrayBuffer = await file.arrayBuffer();
        const zip = await JSZip.loadAsync(arrayBuffer);
        let extractedText = "";

        // Loop through each slide file inside ppt/slides/
        const slideFiles = Object.keys(zip.files).filter((fileName) => fileName.startsWith("ppt/slides/") && fileName.endsWith(".xml"));

        for (const slideFile of slideFiles) {
            const xmlContent = await zip.files[slideFile].async("text");
            const slideData = await parseStringPromise(xmlContent);

            // Extract text from the slide's XML structure
            const texts = [];
            if (slideData["p:sld"] && slideData["p:sld"]["p:cSld"] && slideData["p:sld"]["p:cSld"][0]["p:spTree"]) {
                const shapes = slideData["p:sld"]["p:cSld"][0]["p:spTree"][0]["p:sp"] || [];
                shapes.forEach((shape) => {
                    if (shape["p:txBody"] && shape["p:txBody"][0]["a:p"]) {
                        shape["p:txBody"][0]["a:p"].forEach((paragraph) => {
                            if (paragraph["a:r"] && paragraph["a:r"][0]["a:t"]) {
                                texts.push(paragraph["a:r"][0]["a:t"][0]);
                            }
                        });
                    }
                });
            }

            extractedText += texts.join("\n") + "\n";
        }

        return extractedText.trim() || "No text found in the PowerPoint file.";
    } catch (error) {
        console.error("Error extracting text from PowerPoint file:", error);
        return "Error extracting text from PowerPoint document.";
    }
};

/**
 * Extracts text from an Excel (.xlsx or .xls) file.
 * @param {File} file - The Excel file to process.
 * @returns {Promise<string>} - Extracted text.
 */
export const extractTextFromExcel = async (file) => {
    try {
        const arrayBuffer = await file.arrayBuffer();
        const workbook = XLSX.read(arrayBuffer, { type: "array" });

        let extractedText = "";

        workbook.SheetNames.forEach((sheetName) => {
            const sheet = workbook.Sheets[sheetName];
            const jsonData = XLSX.utils.sheet_to_json(sheet, { header: 1 });

            jsonData.forEach((row) => {
                extractedText += row.join("\t") + "\n";
            });
        });

        return extractedText.trim() || "No text found in the Excel file.";
    } catch (error) {
        console.error("Error extracting text from Excel file:", error);
        return "Error extracting text from Excel document.";
    }
};

