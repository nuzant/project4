// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: db.proto

package iiis.systems.os.blockchaindb;

public interface BlockOrBuilder extends
    // @@protoc_insertion_point(interface_extends:blockdb.Block)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 BlockID = 1;</code>
   */
  int getBlockID();

  /**
   * <code>string PrevHash = 2;</code>
   */
  java.lang.String getPrevHash();
  /**
   * <code>string PrevHash = 2;</code>
   */
  com.google.protobuf.ByteString
      getPrevHashBytes();

  /**
   * <code>repeated .blockdb.Transaction Transactions = 3;</code>
   */
  java.util.List<iiis.systems.os.blockchaindb.Transaction> 
      getTransactionsList();
  /**
   * <code>repeated .blockdb.Transaction Transactions = 3;</code>
   */
  iiis.systems.os.blockchaindb.Transaction getTransactions(int index);
  /**
   * <code>repeated .blockdb.Transaction Transactions = 3;</code>
   */
  int getTransactionsCount();
  /**
   * <code>repeated .blockdb.Transaction Transactions = 3;</code>
   */
  java.util.List<? extends iiis.systems.os.blockchaindb.TransactionOrBuilder> 
      getTransactionsOrBuilderList();
  /**
   * <code>repeated .blockdb.Transaction Transactions = 3;</code>
   */
  iiis.systems.os.blockchaindb.TransactionOrBuilder getTransactionsOrBuilder(
      int index);

  /**
   * <code>string MinerID = 4;</code>
   */
  java.lang.String getMinerID();
  /**
   * <code>string MinerID = 4;</code>
   */
  com.google.protobuf.ByteString
      getMinerIDBytes();

  /**
   * <code>string Nonce = 5;</code>
   */
  java.lang.String getNonce();
  /**
   * <code>string Nonce = 5;</code>
   */
  com.google.protobuf.ByteString
      getNonceBytes();
}
