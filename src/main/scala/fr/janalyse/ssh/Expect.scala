package fr.janalyse.ssh

case class Expect(
  when: (String) => Boolean,
  send: String
  )
 