global.URL.createObjectURL = jest.fn(() => "mock-url");
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import FileUploadPage from "../pages/email_page";
import * as textExtraction from "../utils/textExtraction";
import axios from "axios";

// Mock child components
jest.mock("../components/email_EmailContent", () => ({ content }) => <div data-testid="email-content">{content}</div>);
jest.mock("../components/email_ExtractedEvent", () => ({ emailData }) => <div data-testid="email-events">{emailData.length} events</div>);

// Mock text extraction utilities
jest.mock("axios");

describe("FileUploadPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders initial state with upload prompt", () => {
    render(<FileUploadPage />);
    expect(screen.getByText(/drag & drop a file here/i)).toBeInTheDocument();
  });

  it("handles .msg file upload and triggers event extraction", async () => {
    const fakeFile = new File(["dummy content"], "email.msg", { type: "application/vnd.ms-outlook" });

    jest.spyOn(textExtraction, "parseMsgEmail").mockResolvedValue({
      subject: "Test Subject",
      sender: "test@example.com",
      dates: { receivedDate: "2025-03-20" },
      attachments: [],
      body: "Hello world",
    });

    axios.post.mockResolvedValue({
      data: [
        {
          title: "Event Title",
          description: "Event description",
          all_day_event: false,
          start_date: "2025-03-25",
          start_time: "10:00",
        },
      ],
    });

    const { container } = render(<FileUploadPage />);
    const input = container.querySelector('input[type="file"]');

    await waitFor(() => {
      fireEvent.change(input, { target: { files: [fakeFile] } });
    });

    await waitFor(() => {
      expect(screen.getByTestId("email-content")).toBeInTheDocument();
      expect(screen.getByTestId("email-events")).toHaveTextContent("1 events");
    });
  });

  it("shows image preview and extracted content for image upload", async () => {
    const fakeImage = new File(["image data"], "screenshot.png", { type: "image/png" });

    jest.spyOn(textExtraction, "extractTextFromImage").mockResolvedValue("Extracted text from image");

    axios.post.mockResolvedValue({ data: [] });

    const { container } = render(<FileUploadPage />);
    const input = container.querySelector('input[type="file"]');

    await waitFor(() => {
      fireEvent.change(input, { target: { files: [fakeImage] } });
    });

    await waitFor(() => {
      expect(screen.getByTestId("email-content")).toHaveTextContent("Extracted text from image");
    });
  });

  it("handles unsupported file type", async () => {
    const fakeFile = new File(["unsupported"], "file.xyz", { type: "text/plain" });

    const { container } = render(<FileUploadPage />);
    const input = container.querySelector('input[type="file"]');

    await waitFor(() => {
      fireEvent.change(input, { target: { files: [fakeFile] } });
    });

    await waitFor(() => {
      expect(screen.getByTestId("email-content")).toHaveTextContent("Unsupported file type.");
    });
  });
});
