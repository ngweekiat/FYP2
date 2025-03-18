import { useState, useCallback } from "react";
import MsgReader from "@kenjiuno/msgreader";

export default function Home() {
  const [emailData, setEmailData] = useState(null);
  const [attachments, setAttachments] = useState([]);

  const handleFile = useCallback((file) => {
    const reader = new FileReader();

    reader.onload = (event) => {
      const arrayBuffer = event.target.result;

      const msgReader = new MsgReader(arrayBuffer);
      const msgData = msgReader.getFileData();

      console.log("Extracted Email Data:", msgData);
      setEmailData(msgData);

      if (msgData.attachments && msgData.attachments.length > 0) {
        setAttachments(msgData.attachments);
      } else {
        setAttachments([]);
      }
    };

    reader.readAsArrayBuffer(file);
  }, []);

  const onFileChange = (event) => {
    const file = event.target.files[0];
    if (file) handleFile(file);
  };

  const onDrop = (event) => {
    event.preventDefault();
    const file = event.dataTransfer.files[0];
    if (file) handleFile(file);
  };

  const allowDragOver = (event) => {
    event.preventDefault();
  };

  return (
    <div className="p-5 max-w-xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">📧 Email Extractor Dashboard</h1>

      <div
        onDrop={onDrop}
        onDragOver={allowDragOver}
        className="border-4 border-dashed border-gray-300 rounded-md p-8 text-center cursor-pointer mb-4"
      >
        Drag & Drop your email (.msg) file here 🚀
        <br />
        <span className="text-gray-500 text-sm">
          or select using the file input below:
        </span>
        <input
          type="file"
          accept=".msg"
          onChange={onFileChange}
          className="mt-4"
        />
      </div>

      {emailData && (
        <div className="mt-6 p-4 border rounded shadow">
          <h2 className="text-xl font-semibold mb-2">📨 Email Content</h2>
          <p>
            <strong>Subject:</strong> {emailData.subject}
          </p>
          <p>
            <strong>From:</strong> {emailData.senderName} &lt;{emailData.senderEmail}&gt;
          </p>
          <p>
            <strong>To:</strong>{" "}
            {emailData.recipients.map((recipient, idx) => (
              <span key={idx}>
                {recipient.name} &lt;{recipient.email}&gt;{" "}
              </span>
            ))}
          </p>
          <p>
            <strong>Date:</strong> {emailData.date}
          </p>
          <p className="mt-3 whitespace-pre-wrap">
            <strong>Body:</strong> {emailData.body}
          </p>

          {attachments.length > 0 && (
            <div className="mt-4">
              <h3 className="font-semibold">📎 Attachments:</h3>
              <ul className="list-disc list-inside">
                {attachments.map((att, idx) => (
                  <li key={idx}>
                    {att.fileName} ({(att.contentLength / 1024).toFixed(2)} KB)
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
