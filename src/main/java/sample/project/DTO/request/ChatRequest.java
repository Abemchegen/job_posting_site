package sample.project.DTO.request;

public record ChatRequest(String senderName, String recieverName, String message, long receiverID, long senderID) {

}
