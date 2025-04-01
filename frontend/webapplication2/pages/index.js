import { useState } from "react";
import { motion } from "framer-motion";
import { useDropzone } from "react-dropzone";
import { Plus } from "lucide-react";
import EmailContent from "../components/email_EmailContent";
import EmailEvents from "../components/email_ExtractedEvent";
import {
  extractTextFromImage,
  parseMsgEmail,
  extractTextFromPptx,
  extractTextFromExcel,
} from "../utils/textExtraction";
import { sendToGemini } from "../utils/geminiApi"; // ✅ Gemini API

export default function FileUploadPage() {
  const [file, setFile] = useState(null);
  const [uploaded, setUploaded] = useState(false);
  const [emailContent, setEmailContent] = useState("");
  const [imagePreview, setImagePreview] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [extractedEvents, setExtractedEvents] = useState([]);
  const [textInput, setTextInput] = useState("");

  const processWithGemini = async (text) => {
    setEmailContent(text);
    const events = await sendToGemini(text);
    if (Array.isArray(events)) {
      setExtractedEvents(events);
    }
  };

  const onDrop = async (acceptedFiles) => {
    const newFile = acceptedFiles[0];
    setFile(newFile);
    setUploaded(true);

    const fileType = newFile.name.split(".").pop().toLowerCase();
    let text = "";

    try {
      if (["jpg", "jpeg", "png"].includes(fileType)) {
        setImagePreview(URL.createObjectURL(newFile));
        setIsProcessing(true);
        text = await extractTextFromImage(newFile);
        setIsProcessing(false);
      } else if (fileType === "msg") {
        const emailData = await parseMsgEmail(newFile);
        text = `Subject: ${emailData.subject}\nFrom: ${emailData.sender}\nReceived Date: ${JSON.stringify(emailData.dates, null, 2)}\nAttachments: ${emailData.attachments}\n\nEmail Body: ${emailData.body}`;
      } else if (fileType === "pptx") {
        setIsProcessing(true);
        text = await extractTextFromPptx(newFile);
        setIsProcessing(false);
      } else if (["xlsx", "xls", "csv"].includes(fileType)) {
        setIsProcessing(true);
        text = await extractTextFromExcel(newFile);
        setIsProcessing(false);
      } else {
        text = "Unsupported file type.";
      }

      console.log("Text data:", text);
      await processWithGemini(text);
    } catch (err) {
      console.error("❌ Error processing file:", err);
    }
  };

  const handleDiscard = () => {
    setFile(null);
    setUploaded(false);
    setEmailContent("");
    setImagePreview(null);
    setExtractedEvents([]);
    setTextInput("");
  };

  const { getRootProps, getInputProps } = useDropzone({ onDrop });

  return (
    <div className="flex flex-col h-screen w-full p-4 bg-gray-100 items-center">
      <h1 className="text-3xl font-bold text-gray-800 mb-4 w-full text-left">
        Calendar Event Extraction Tool
      </h1>

      {!uploaded ? (
        <div className="flex flex-col items-center w-full">
          <div
            {...getRootProps()}
            className="border-2 border-dashed border-gray-400 p-12 text-center cursor-pointer rounded-lg bg-white shadow-lg flex flex-col items-center justify-center w-1/3 hover:bg-gray-50 transition"
          >
            <input {...getInputProps()} />
            <Plus className="h-12 w-12 text-gray-500 mb-3" />
            <p className="text-gray-600 font-semibold">Drag & drop a file here</p>
            <p className="text-sm text-gray-500">or click to select a file</p>
            <p className="text-xs text-gray-500 mt-2">Or paste an image</p>
          </div>

          {/* Manual text input area */}
          <div className="w-1/3 mt-6">
            <label className="block text-gray-700 font-semibold mb-2" htmlFor="manualText">
              Or paste your email/text content here:
            </label>
            <textarea
              id="manualText"
              rows={6}
              value={textInput}
              onChange={(e) => setTextInput(e.target.value)}
              placeholder="Paste your email or message text here..."
              className="w-full p-3 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none"
            />
            <button
              onClick={async () => {
                setUploaded(true);
                await processWithGemini(textInput);
              }}
              className="mt-3 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
            >
              Send Text
            </button>
          </div>
        </div>
      ) : (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5 }} className="flex w-full">
          <motion.div initial={{ x: 100 }} animate={{ x: 0 }} transition={{ duration: 0.5 }} className="flex flex-col w-1/2 p-4 bg-white rounded-lg shadow-md">
            <div {...getRootProps()} className="border-2 border-dashed border-gray-300 p-10 text-center cursor-pointer rounded-lg bg-gray-50">
              <input {...getInputProps()} />
              <p>{file ? file.name || "Pasted Image" : "Drag & drop a file here or click to upload"}</p>
            </div>

            {imagePreview && (
              <div className="mt-4 flex justify-center">
                <img src={imagePreview} alt="Pasted content" className="max-w-full max-h-60 rounded-lg shadow" />
              </div>
            )}

            {isProcessing && (
              <p className="text-gray-500 text-sm mt-2 text-center">
                Processing image for text extraction...
              </p>
            )}

            <div className="mt-4 flex-grow overflow-auto max-h-180">
              <EmailContent content={emailContent} />
            </div>
          </motion.div>

          <div className="flex-1 p-4 bg-white rounded-lg shadow-md ml-4 h-[900px] overflow-y-auto">
            <EmailEvents onDiscard={handleDiscard} emailData={extractedEvents} />
          </div>
        </motion.div>
      )}
    </div>
  );
}
