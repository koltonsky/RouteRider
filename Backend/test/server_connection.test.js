const fs = require('fs').promises;
//const https = require('https');
const { stopSSLServer, connectToDatabase } = require('../server');

let client;

describe('Server Tests', () => {
    let originalReadFile;
  
    beforeAll(() => {
      // Mock fs.promises.readFile to avoid reading actual files
      originalReadFile = fs.readFile;
      fs.readFile = jest.fn();
    });
  
    afterAll(() => {
      // Restore fs.promises.readFile
      fs.readFile = originalReadFile;
    });
  
    // Input: test_key, certificate
    // Expected status code: n/A
    // Expected behavior: Server should start and stop running
    // ChatGPT usage: Yes

    it('should start the SSL server', async () => {
      // Mock the fs.promises.readFile responses
      fs.readFile.mockResolvedValueOnce(Buffer.from('test_key_content'));
      fs.readFile.mockResolvedValueOnce(Buffer.from('certificate_content'));
  
      // Call the function
      await connectToDatabase().then(() => {
        return stopSSLServer();
      });
  
    });
  });