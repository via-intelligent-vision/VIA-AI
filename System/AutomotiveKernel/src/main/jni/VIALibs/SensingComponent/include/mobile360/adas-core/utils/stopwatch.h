#ifndef Stopwatch_H
#define Stopwatch_H
#include <chrono>
#include "mobile360/adas-core/utils/adas-logger.h"

using namespace std::chrono;
class Stopwatch {
  public:
    explicit Stopwatch(bool start);
    explicit Stopwatch(char const* activity = "Stopwatch",
                       bool start=true);
    ~Stopwatch();

    unsigned long LapGet() const;
    bool IsStarted() const;
    void Show();
    void Start();
    void Stop();

  private:  
    unsigned long GetMs() const ; 
    char const *activity_; 
    std::chrono::system_clock::time_point start_;

};

inline Stopwatch::Stopwatch(bool start_now) 
    : activity_("Stopwatch")
    , start_(system_clock::time_point::min()) {
    if (start_now) {
        Start();
    }
}

inline Stopwatch::Stopwatch(char const* activity, bool start_now)
  : activity_(activity && activity[0] ? activity : nullptr) 
  , start_(system_clock::time_point::min()) {
    if (start_now) {
        Start();
    }
}

inline Stopwatch::~Stopwatch() {
    if (IsStarted()) {
        Stop();
    }
}

inline bool Stopwatch::IsStarted() const {
    return (start_ != system_clock::time_point::min());
}

inline unsigned long Stopwatch::GetMs() const {
    if (IsStarted()) {
        system_clock::duration diff;
        diff = system_clock::now() - start_;
        return (unsigned)(duration_cast<milliseconds>(diff).count());
    }
    return 0;
}

inline unsigned long Stopwatch::LapGet() const {
    return GetMs();
}

inline void Stopwatch::Show() {
    std::ostringstream log;

    if (IsStarted()) {
        if (activity_)
            log << activity_ << ": ";
        log << "show " << "at " << GetMs() << "ms";
    }
    else {
        if (activity_)
            log << activity_ << ": ";
         log << "not started";
    }
    AdasDebug("Stopwatch", "%s", log.str().c_str());

    return;
}

inline void Stopwatch::Start() {
    std::ostringstream log;

    if (IsStarted()) {
        Stop();
    }
    else {
        if (activity_)
            log << activity_ << ": ";
        log << "start";
        AdasDebug("Stopwatch", "%s", log.str().c_str());
    }
    start_ = std::chrono::system_clock::now();
    return;
}

inline void Stopwatch::Stop() {
    std::ostringstream log;

    if (IsStarted()) {
        if (activity_)
            log << activity_ << ": ";
        log << "stop " << GetMs() << "ms";
        AdasDebug("Stopwatch", "%s", log.str().c_str());
    }

    start_ = system_clock::time_point::min(); 
    return;
}
# endif

